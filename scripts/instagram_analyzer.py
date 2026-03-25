import sys
import os
import json
import instaloader
from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer

# Suppress stderr to avoid polluting JSON output for Java
sys.stderr = open(os.devnull, 'w')

def analyze_sentiment(caption):
    if not caption:
        return "neutral"
    analyzer = SentimentIntensityAnalyzer()
    compound = analyzer.polarity_scores(caption)['compound']
    if compound >= 0.05: return "positive"
    elif compound <= -0.05: return "negative"
    else: return "neutral"

def main():
    if len(sys.argv) < 2:
        print(json.dumps({"error": "Hashtag argument missing"}))
        sys.exit(1)
        
    hashtag = sys.argv[1].replace("#", "")
    
    L = instaloader.Instaloader(
        quiet=True,
        download_pictures=False,
        download_video_thumbnails=False,
        download_videos=False,
        download_geotags=False,
        download_comments=False,
        save_metadata=False,
        compress_json=False
    )
    # Set a browser-like user agent to help bypass simple blocks
    L.context.user_agent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36'
    
    # Check for session just in case
    import glob
    windows_path = os.path.join(os.environ.get('LOCALAPPDATA', ''), 'Instaloader', 'session-*')
    unix_path = os.path.join(os.path.expanduser('~'), '.config', 'instaloader', 'session-*')
    session_files = glob.glob(windows_path) + glob.glob(unix_path)
    
    if session_files:
        session_file = session_files[0]
        auth_username = os.path.basename(session_file).replace('session-', '')
        try:
            L.load_session_from_file(auth_username)
        except Exception:
            pass

    posts_data = []
    positive_count = 0
    negative_count = 0
    neutral_count = 0
    users_freq = {}
    
    try:
        hashtag_iterator = instaloader.Hashtag.from_name(L.context, hashtag).get_posts()
        
        count = 0
        for post in hashtag_iterator:
            if count >= 20: 
                break
                
            username = post.owner_username
            caption = post.caption if post.caption else ""
            sentiment = analyze_sentiment(caption)
            likes = post.likes
            url = f"https://www.instagram.com/p/{post.shortcode}/"
            
            if sentiment == "positive":
                positive_count += 1
            elif sentiment == "negative":
                negative_count += 1
            else:
                neutral_count += 1
                
            users_freq[username] = users_freq.get(username, 0) + 1
            
            posts_data.append({
                "username": username,
                "caption": caption[:200] + "..." if len(caption) > 200 else caption,
                "sentiment": sentiment,
                "likes": likes,
                "url": url
            })
            
            count += 1
            
        top_accounts = sorted(users_freq.items(), key=lambda x: x[1], reverse=True)[:5]
        top_accounts_list = [{"username": acc[0], "post_count": acc[1]} for acc in top_accounts]
        
        result = {
            "hashtag": hashtag,
            "total_posts": len(posts_data),
            "analysis": {
                "positive": positive_count,
                "negative": negative_count,
                "neutral": neutral_count
            },
            "posts": posts_data,
            "top_accounts": top_accounts_list
        }
        
        print(json.dumps(result))
        
    except Exception as e:
        # Output the real exception block reason back to the user interface
        str_err = str(e).replace('"', "'")
        print(json.dumps({"error": f"Instagram blocked the scraper (IP Rate Limit or Login Required): {str_err}."}))
        sys.exit(1)

if __name__ == "__main__":
    main()
