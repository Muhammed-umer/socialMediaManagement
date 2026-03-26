import sys
import json
import os
import requests
from youtube_transcript_api import YouTubeTranscriptApi
from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer

# Suppress stderr to avoid polluting JSON output for Java
sys.stderr = open(os.devnull, 'w')

def analyze_sentiment(text):
    if not text:
        return "neutral"
    analyzer = SentimentIntensityAnalyzer()
    compound = analyzer.polarity_scores(text)['compound']
    if compound >= 0.05: return "positive"
    elif compound <= -0.05: return "negative"
    else: return "neutral"

def summarize_text(text, api_key):
    if not text or len(text) < 50:
        return text
    
    API_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-cnn"
    headers = {"Authorization": f"Bearer {api_key}"}
    
    # HF Bart has a typical max length of 1024 tokens. Truncating raw text to ~3000 chars to be safe
    truncated_text = text[:3000]
    
    payload = {
        "inputs": truncated_text,
        "parameters": {"max_length": 300, "min_length": 80, "do_sample": False}
    }
    
    import time
    for attempt in range(2):
        try:
            response = requests.post(API_URL, headers=headers, json=payload, timeout=30)
            if response.status_code == 200:
                result = response.json()
                if isinstance(result, list) and len(result) > 0 and 'summary_text' in result[0]:
                    return result[0]['summary_text']
            elif response.status_code == 503:
                # Model is loading
                result = response.json()
                if 'estimated_time' in result:
                    wait_time = min(result['estimated_time'], 20.0)
                    time.sleep(wait_time)
                    continue
        except Exception as e:
            pass
        break
    
    return truncated_text[:200] + "..."

def main():
    if len(sys.argv) < 5:
        print(json.dumps({"error": "Arguments missing: <video_id> <title> <description> <hf_api_key>"}))
        sys.exit(1)
        
    video_id = sys.argv[1]
    title = sys.argv[2]
    description = sys.argv[3]
    hf_api_key = sys.argv[4]
    
    text_content = ""
    try:
        transcript_list = YouTubeTranscriptApi.list_transcripts(video_id)
        try:
            transcript = transcript_list.find_transcript(['en'])
        except Exception:
            # If English doesn't exist, translate the first available one
            for t in transcript_list:
                transcript = t.translate('en')
                break
        text_content = " ".join([t['text'] for t in transcript.fetch()])
    except Exception:
        # Fallback if transcript is disabled or not found
        text_content = f"{title}. {description}"
    
    if not text_content.strip():
        print(json.dumps({"summary": "No content available.", "sentiment": "neutral"}))
        return

    sentiment = analyze_sentiment(text_content)
    summary = summarize_text(text_content, hf_api_key)
    
    result = {
        "sentiment": sentiment,
        "summary": summary
    }
    
    # Re-enable stdout to print the JSON result
    print(json.dumps(result))

if __name__ == "__main__":
    main()
