# Introduction
This sample app shows the social login with LinkedIn using OAuth 2.0 in kotlin for Android. The sample requires Android Studio and can run on Android SDK version 16 and above.

# Get Application credentials after creating app in LinkedIn developer account
login to LinkedIn developer and create app from https://www.linkedin.com/developers/.
Fill details and create the app.
Go to `Auth` by selecting the second tab of created app and get
* Client ID
* Client Secret
* Redirect URL

# Modify the credentials to login successfully
Replace the credentials in project to login.
Replace the **client_id**, **client_secret** and **redirect_url** with original credential and run the app.

# Implement the LinkedIn login to any of your project
For signing in with LinkedIn in your project, copy the `loginwithlinkedin-release.aar` from libs folder of this project to your project. Also, add `implementation files('libs/loginwithlinkedin-release.aar')` in dependencies of your app's gradle.
