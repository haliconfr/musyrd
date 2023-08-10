# musyrd
android app template for posting reviews

![sc4](https://github.com/maxtearney/musyrd/assets/88261993/ad352e92-0128-4615-956e-a4748eeac874) ![sc3](https://github.com/maxtearney/musyrd/assets/88261993/28565e9f-7a08-48ac-b418-2ac8fee91d4e) ![sc2](https://github.com/maxtearney/musyrd/assets/88261993/322b6b33-2820-410f-852e-f32c6d3fcffc)


currently built for music reviews, but you can use it for any other subject (ie. movie reviews, restaurant reviews)

! to use this app you need an algolia search account and a firebase and firebase storage account !

db.collection("users") contains all user data in text form,
db.collection("releases") contains all content that reviews can be posted of


Features:

- The app contains an user profile that shows a list of people the current user follows and who follows them, and an editable profile photo and biography.

- The search function features the ability to upload releases to the app's firebase database, and can add metadata such as the release's title, name of the artist and album art. The user can also either search the database for other users or releases using this screen. (explore.xml)

- The app scans the current user's account and can find other users that have reviewed the same things. A percentage similarity of both user's posted reviews shows at the top of main_app.xml

- Users can delete reviews they've posted using the bin icon in the corner of their review.

- Reviews include the star rating, username of the reviewer, review text and the date it was posted.

- Tapping the name of the subject on reviews loads a page featuring other reviews of the same subject, along with an average star rating at the top of the page.

- Tapping the user's name on review cards loads their profile.

- All strings used in the app are in strings.xml so you can easily change them if you want.

- Fully functional login screen making it easy for users to create and login to accounts using their username, email and password.

- The ability to follow users and subjects, which makes all reviews from those you follow appear on main_app.xml

- The ability to log out of your current account using the dropdown menu at the top of main_app.xml

- The ability for users to report other users and subjects, which adds them to a list in the admin's firebase database, making it easy to ban the offending account/delete the offending release.

- The ability to block other users, causing that user to never see your reviews you to never see theirs.



feel free to use any code/visual assets for anything! no need for credit :)
