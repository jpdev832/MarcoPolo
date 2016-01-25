# MarcoPolo
Marco Polo is geofence and exploration app for sharing moments with friends with the help of Uber Puff. Who is Uber Puff!?

![](http://www.staticvillage.com/apps/marco_polo/uber_puff_banner.png)

Uber Puff is a map assistant that will guide users from one marker to the next while showing them special moments on the way. She loves polaroids and has a pretty good knowledge of android eco system. So, she can even send users to moments in other apps, on web pages, or even play that special song at the right moment.

#How To Use
The Marco Polo app has few key components that need to be updated in order to run though.

1. Google Maps Api Key
  * The res/values/google_maps_api.xml file will need to be updated with your api key
2. REST endpoint for saving images
  * A POST endpoint will need to be added to /res/values/rest_endpoint.xml that can process Multipart/form-data http request
3. REST endpoint for saving marker file
  * A POST endpoint will need to be added to /res/values/rest_endpoint.xml that can parse and save a json representation of a marker set

#Request Format

#Response Format

#Server Side Example
