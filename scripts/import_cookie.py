import instaloader
import sys

def inject_session(username, sessionid):
    print(f"Injecting sessionid for user {username}...")
    L = instaloader.Instaloader()
    
    # Inject the session cookie into the requests session
    L.context._session.cookies.set("sessionid", sessionid, domain=".instagram.com")
    L.context.username = username
    
    try:
        # Blindly save it to the exact location instagram_analyzer.py expects without verifying
        L.save_session_to_file()
        print("Success! The authenticated session has been saved and is ready for scraping.")
    except Exception as e:
        print("Failed to authenticate with the provided sessionid. Is it expired or copied incorrectly?")
        print("Error details:", e)

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python import_cookie.py <username> <sessionid>")
        sys.exit(1)
        
    inject_session(sys.argv[1], sys.argv[2])
