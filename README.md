# MarcoPolo
Marco Polo is geofence and exploration app for sharing moments with friends with the help of Uber Puff. Who is Uber Puff!?

![](http://www.staticvillage.com/apps/marco_polo/uber_puff_banner.png)

Uber Puff is a map assistant that will guide users from one marker to the next while showing them special moments on the way. She loves polaroids and has a pretty good knowledge of android eco system. So, she can even send users to moments in other apps, on web pages, or even play that special song at the right moment.

#How To Use
The Marco Polo app has few key components that need to be updated in order to run though.

* Google Maps Api Key
  * The res/values/google_maps_api.xml file will need to be updated with your api key
```xml
<string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">
  <Api Key Here>
</string>
```
* REST endpoint for saving images
  * A POST endpoint will need to be added to /res/values/rest_endpoint.xml that can process Multipart/form-data http request
```xml
<string name="post_simage">http://www.website.com/apps/marco_polo/image_upload.php</string>
```
* REST endpoint for saving marker file
  * A POST endpoint will need to be added to /res/values/rest_endpoint.xml that can parse and save a json representation of a marker set
```xml
<string name="post_marker">http://www.website.com/apps/marco_polo/marker_upload.php</string>
</string>
```

#Request Format
Marker sets are sent in the following format with a name representing the set and the marker data represented as json
```json
{
  "name":"our_moment",
  "data":"[
    {
      \"data\":[],
      \"latitude\":40.72077279768406,
      \"longitude\":-73.98633845150471,
      \"markerIndex\":0,
      \"message\":\"Start here and folow my markers\",
      \"radius\":50,
      \"timestamp\":1453173046375,
      \"type\":\"Image\",
      \"id\":38,
      \"tableName\":\"POINT_MARKER\"
    },
    {
      \"data\":[
        \"http://www.website.com/apps/marco_polo/images/moment4.jpg\",
        \"http://www.website.com/apps/marco_polo/images/moment5.jpg\"
      ],
      \"latitude\":40.72108585438302,
      \"longitude\":-73.98686282336712,
      \"markerIndex\":1,
      \"message\":\"This is the first place we met!\",
      \"radius\":50,
      \"timestamp\":1453212204884,
      \"type\":\"Image\",
      \"id\":39,
      \"tableName\":\"POINT_MARKER\"
    }
  ]"
}
```

#Response Format
The response code from the sever should return a json response with the following format.
```json
{
  "status":"SUCCESS",
  "status_code":200,
  "message":"saved",
  "path":"http://www.website.com/apps/marco_polo/moment1.png"
}
```

#PHP Server Side Example (Image Save)
```php
<?php
$web      = 'http://www.website.com'.dirname($_SERVER['PHP_SELF']).DIRECTORY_SEPARATOR.'markers';
$filePath = getcwd().DIRECTORY_SEPARATOR.'markers';
$method   = $_SERVER['REQUEST_METHOD'];

$output = array("status" => "FAIL",
                "status_code" => 400,
                "message" => "",
                "path" => "");

switch($method) {
    case 'POST':
        $json = json_decode(file_get_contents('php://input'), true);
        $name = $json['name'];
        $data = $json['data'];
        
        if(empty($name)) {
            $output['message'] = "Bad Data! Error in name";
            die(json_encode($output));
        }
        
        if(empty($data)) {
            $output['message'] = "Bad Data! Error in data";
            die(json_encode($output));
        }
    
        $file = $filePath.DIRECTORY_SEPARATOR.$name.'.mjs';
        $web .= DIRECTORY_SEPARATOR.$name.'.mjs';
        if(!file_put_contents($file, $data, LOCK_EX)) {
            if(file_exists($file)) {
                $output['message'] = "File already exists!";
            } else {
                $output['message'] = "An Error Occurred";
            }
        } else {
            $output['status'] = 'SUCCESS';
            $output['status_code'] = 200;
            $output['message'] = "Saved!";
            $output['path'] = $web;
        }
        break;
    default:
        header('HTTP/1.1 405 Method Not Allowed');
        header('Allow: POST');
        break;
}

die(json_encode($output));
?>
```

#PHP Server Side Example (Marker Save)
```php
<?php
$web        = 'http://www.website.com'.dirname($_SERVER['PHP_SELF']).DIRECTORY_SEPARATOR.'images'.DIRECTORY_SEPARATOR;
$target_dir = getcwd().DIRECTORY_SEPARATOR.'images'.DIRECTORY_SEPARATOR;
$method     = $_SERVER['REQUEST_METHOD'];

$output = array("status" => "FAIL",
                "status_code" => 400,
                "message" => "",
                "path" => "");

switch($method) {
    case 'POST':
        $target_file   = $target_dir.basename($_FILES["uploadFile"]["name"]);
        $web           = $web.basename($_FILES["uploadFile"]["name"]);
        $imageFileType = pathinfo($target_file, PATHINFO_EXTENSION);
        
        $check = getimagesize($_FILES["uploadFile"]["tmp_name"]);
        if($check === false) {
            $output["message"] = "Invalid data";
            break;
        }
        
        // Check if file already exists
        if (file_exists($target_file)) {
            $output["status"] = "SUCCESS";
            $output['status_code'] = 200;
            $output['message'] = "File already exists";
            $output['path'] = $web;
            break;
        }

        // Allow certain file formats
        if($imageFileType != "jpg" && $imageFileType != "png" && $imageFileType != "jpeg") {
            $output['status_code'] = 400;
            $output["message"] = "Invalid file format";
            break;
        }
        
        if (move_uploaded_file($_FILES["uploadFile"]["tmp_name"], $target_file)) {
            $output['status_code'] = 201;
            $output['message'] = "File saved";
        } else {
            $output['status_code'] = 400;
            $output['message'] = "There was an error uploading file";
            break;
        }
        
        $output['status'] = 'SUCCESS';
        $output['path'] = $web;
        break;
    default:
        header('HTTP/1.1 405 Method Not Allowed');
        header('Allow: POST');
        break;
}

die(json_encode($output));
?>
```
