# 10 Minute Test

This file contains a manual test suite that should cover the most important functionality of the app and be tested prior to every release to ensure the functionality of the app.


### Testing Instructions

#### Onboarding
- Freshly install the app
- Log in with a User -> Login successful
- Enable Push notifications
- Browse / enroll courses

#### Exercise / Lecture
- Go to a course 
- See exercise details -> problem statement is loaded successfully
- See lecture details

#### Communication
- Go to the communication tab
- Also open the communication for this course on the webapp (with a different user)
- On Android, go to a channel with existing posts -> posts are displayed
- Create a new conversation with webapp user (eg DM or group chat)
- Write a new post -> Post are shown in webapp
- Write a reply -> is sent to server (confirm in webapp)
- In webapp with a different user write a post -> On Android: 
  - When in the same conversation: new post is received and displayed in the chat
  - When not in conversation: notification is received
- Android: react to a post -> reaction is shown on Android and webapp