var style = [
  {
    "elementType": "labels.icon",
    "stylers": [
      { "visibility": "off" }
    ]
  },{
    "elementType": "labels.text",
    "stylers": [
      { "visibility": "on" },
      { "lightness": 51 }
    ]
  },{
    "stylers": [
      { "invert_lightness": true },
      { "gamma": 1.53 },
      { "saturation": -59 }
    ]
  },{
    "featureType": "road.highway",
    "elementType": "geometry",
    "stylers": [
      { "visibility": "on" },
      { "saturation": -45 }
    ]
  }
]

var map;
var poly;
var locationMarkers = {};
var locationPaths = {};

function loadData(){
    var script = document.createElement('script');
    script.setAttribute("type","text/javascript");
    script.setAttribute("src", "./data.json");
    
    document.getElementsByTagName("head")[0].appendChild(script);
    
    window.setTimeout("loadData()", 1000);
}

function newData(data){
    for (var userID in data) {
        locations = data[userID];
        last = locations.pop();
        
        userKey = userID.replace(/-/g,""); //- not allowed in keys?
        
        if(typeof(locationMarkers[userKey]) != 'undefined'){
            locationMarkers[userKey].setMap(null);
        }
        
        locationMarkers[userKey] = new google.maps.Marker({});
        locationMarkers[userKey].setPosition(new google.maps.LatLng(last.lat, last.lon));
        locationMarkers[userKey].setMap(map);
        
        drawMovement(locations, userKey);
    }
}


function initialize() {
    var mapOptions = {
        center: new google.maps.LatLng(49.579838, 11.019883),
        zoom: 15,
        disableDefaultUI: true,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        styles: style
    };
    
    map = new google.maps.Map(document.getElementById("map_canvas"),
        mapOptions);
    
    var ctaLayer = new google.maps.KmlLayer({
        url: 'http://wwwcip.cs.fau.de/~ar79yxiw/journeyData/places.kml'
    });
    ctaLayer.setMap(map);
    
    loadData();
}

function drawMovement(coordList, userID){
    var polyOptions = {
        strokeColor: '#FFFFFF',
        strokeOpacity: 0.70,
        strokeWeight: 1.5
    }
    
    if(typeof(locationPaths[userID]) != 'undefined'){
        var last = coordList.length - 1;
        locationPaths[userID].getPath().push(new google.maps.LatLng(coordList[last].lat, coordList[last].lon));
        return;
    }
    
    locationPaths[userID] = new google.maps.Polyline(polyOptions);
    locationPaths[userID].setMap(map);
    
    for (var i = 0; i < coordList.length; i++) {
        locationPaths[userID].getPath().push(new google.maps.LatLng(coordList[i].lat, coordList[i].lon));
    }
}
