var style_runner = [{
    "elementType": "labels.text",
    "stylers": [{
        "visibility": "off"
    }]
}, {
    "elementType": "labels",
    "stylers": [{
        "visibility": "off"
    }]
}, {
    "stylers": [{
        "invert_lightness": true
    }, {
        "saturation": -36
    }, {
        "hue": "#ffd500"
    }, {
        "lightness": -50
    }, {
        "gamma": 1.72
    }]
}]

var style_chaser = [{
    "elementType": "labels.text",
    "stylers": [{
        "visibility": "off"
    }]
}, {
    "elementType": "labels",
    "stylers": [{
        "visibility": "off"
    }]
}, {
    "stylers": [{
        "invert_lightness": true
    }, {
        "saturation": -36
    }, {
        "lightness": -50
    }, {
        "gamma": 1.72
    }, {
        "hue": "#ff0011"
    }]
}]
var map;
var geolocationMarker;
var lockLocationButton
var locationChangedListener;
var startMarker;
var checkpointMarkers = [];
var safeZones = [];
var offLimitsZones = [];
var poly;

//For debugging outside the app:
if (typeof (comm) == 'undefined') {
    comm = {
        getTheme: function () {
            return "THEME_RUNNER";
        },

        getStart: function () {
            return JSON.stringify({
                lat: 49.579838,
                lon: 11.019883
            })
        },

        getCheckpoints: function () {
            return JSON.stringify([{
                lat: 49.593742,
                lon: 11.010189,
                no: "Checkpoint 1"
            }, {
                lat: 49.599602,
                lon: 11.005383,
                no: "Checkpoint 2"
            }]);
        },

        getSafeZones: function () {
            return JSON.stringify([
                [{
                    lat: 49.579578,
                    lon: 11.020153
                }, {
                    lat: 49.579628,
                    lon: 11.018418
                }, {
                    lat: 49.579727,
                    lon: 11.017195
                }, {
                    lat: 49.580902,
                    lon: 11.017439
                }, {
                    lat: 49.581490,
                    lon: 11.017554
                }, {
                    lat: 49.582077,
                    lon: 11.017610
                }, {
                    lat: 49.581944,
                    lon: 11.020341
                }, {
                    lat: 49.579578,
                    lon: 11.020153
                }],
                [{
                    lat: 49.588337,
                    lon: 11.002341
                }, {
                    lat: 49.588799,
                    lon: 11.002515
                }, {
                    lat: 49.589359,
                    lon: 11.002555
                }, {
                    lat: 49.589443,
                    lon: 11.003440
                }, {
                    lat: 49.589603,
                    lon: 11.003400
                }, {
                    lat: 49.589657,
                    lon: 11.004076
                }, {
                    lat: 49.589699,
                    lon: 11.004073
                }, {
                    lat: 49.589737,
                    lon: 11.004508
                }, {
                    lat: 49.589771,
                    lon: 11.004832
                }, {
                    lat: 49.589542,
                    lon: 11.004919
                }, {
                    lat: 49.589455,
                    lon: 11.003905
                }, {
                    lat: 49.588783,
                    lon: 11.004041
                }, {
                    lat: 49.588764,
                    lon: 11.003633
                }, {
                    lat: 49.588272,
                    lon: 11.003655
                }, {
                    lat: 49.588268,
                    lon: 11.003239
                }, {
                    lat: 49.588337,
                    lon: 11.002341
                }]
            ]);
        },

        getOffLimitsZones: function () {
            return JSON.stringify([
                [{
                    lat: 49.587025,
                    lon: 11.028275
                }, {
                    lat: 49.586830,
                    lon: 11.027459
                }, {
                    lat: 49.586273,
                    lon: 11.026944
                }, {
                    lat: 49.585579,
                    lon: 11.026773
                }, {
                    lat: 49.585495,
                    lon: 11.024605
                }, {
                    lat: 49.582561,
                    lon: 11.026687
                }, {
                    lat: 49.583603,
                    lon: 11.026816
                }, {
                    lat: 49.583519,
                    lon: 11.029283
                }, {
                    lat: 49.582783,
                    lon: 11.029369
                }, {
                    lat: 49.582268,
                    lon: 11.028983
                }, {
                    lat: 49.582157,
                    lon: 11.032330
                }, {
                    lat: 49.585842,
                    lon: 11.032116
                }, {
                    lat: 49.586147,
                    lon: 11.031687
                }, {
                    lat: 49.586720,
                    lon: 11.031450
                }, {
                    lat: 49.587025,
                    lon: 11.028275
                }]
            ])
        },
        
        isFollowing: false,
        
        setFollowingUser: function (fu) {
            this.isFollowing = fu
        },

        isFollowingUser: function () {
            return this.isFollowing;
        },
        
        getPreviousMovement: function(){
            return JSON.stringify(
                [{ 
                    lat: 49.57358579558048,
                    lon:11.024158000946045
                }, { 
                    lat: 49.57478940023296,
                    lon:11.026378870010376
                }, {
                    lat: 49.57471287134099,
                    lon:11.026604175567627
                }, { 
                    lat: 49.57455981319708,
                    lon:11.026754379272461
                }, { 
                    lat: 49.57448328394513,
                    lon:11.027065515518188
                }, { 
                    lat: 49.57435109677272,
                    lon:11.027108430862427
                }, {
                    lat: 49.5729526737061 ,
                    lon:11.02502703666687 
                }]
            );
        },
        
        isServiceRunning: function (){
            return true;
        }
    };
}

function initialize() {
    onStart();
    onResume();
}

function onStart() {
    var start = JSON.parse(comm.getStart());

    var mapOptions = {
        center: new google.maps.LatLng(start.lat, start.lon),
        zoom: 15,
        disableDefaultUI: true,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    map = new google.maps.Map(document.getElementById("map_canvas"),
        mapOptions);

    drawStart(start.lat, start.lon);
    drawCheckpoints(JSON.parse(comm.getCheckpoints()));
    drawSafezones(JSON.parse(comm.getSafeZones()));
    drawOffLimitsZones(JSON.parse(comm.getOffLimitsZones()));
    drawPreviousMovement(JSON.parse(comm.getPreviousMovement()));

    geomarker = new GeolocationMarker();
    geomarker.setMap(map);

    drawMovementListener = google.maps.event.addListener(geomarker, "position_changed", function () {
        if(comm.isServiceRunning()){
            poly.getPath().push(this.getPosition());
        }
    });
   
    var homeControlDiv = document.createElement('div');
    var homeControl = new lockLocationControl(homeControlDiv, map);

    homeControlDiv.index = 1;
    map.controls[google.maps.ControlPosition.TOP_LEFT].push(homeControlDiv);
}

function onResume() {
    setTheme(comm.getTheme());
    setFollowingUser(comm.isFollowingUser());
    //fill in gap with data from service
    removePreviousMovement();
    drawPreviousMovement(JSON.parse(comm.getPreviousMovement()));
    geomarker.startWatch();
}

function onDisplay() {
    setFollowingUser(comm.isFollowingUser());
}

function onPause() {
    geomarker.stopWatch();
}

function setTheme(theme) {
    if (theme == "THEME_CHASER") {
        map.setOptions({
            styles: style_chaser
        });
    } else if (theme == "THEME_RUNNER") {
        map.setOptions({
            styles: style_runner
        });
    }
}

function drawStart(lat, long) {
    var markerOptions = {
        position: new google.maps.LatLng(lat, long),
        clickable: false,
        map: map,
        icon: {
            path: google.maps.SymbolPath.CIRCLE,
            strokeColor: '#FFFFFF',
            scale: 5
        },
        labelContent: "Start/Ziel",
        labelAnchor: new google.maps.Point(50, -10),
        labelClass: "labels",
        labelInBackground: false
    }

    startMarker = new MarkerWithLabel(markerOptions);
}

function drawCheckpoints(checkpoints) {
    for (var i = 0; i < checkpoints.length; i++) {
        drawCheckpoint(checkpoints[i].lat, checkpoints[i].lon, checkpoints[i].no);
    }
}

function drawCheckpoint(lat, long, name) {
    var markerOptions = {
        position: new google.maps.LatLng(lat, long),
        clickable: false,
        map: map,
        icon: {
            path: google.maps.SymbolPath.CIRCLE,
            strokeColor: '#FFFFFF',
            scale: 5
        },
        labelContent: name,
        labelAnchor: new google.maps.Point(50, -10),
        labelClass: "labels",
        labelInBackground: false
    }

    checkpointMarkers.push(new MarkerWithLabel(markerOptions));
}

function drawSafezones(safezones) {
    for (var i = 0; i < safezones.length; i++) {
        drawSafezone(safezones[i]);
    }
}

function drawSafezone(coordList) {

    coords = [];
    for (var i = 0; i < coordList.length; i++) {
        coords.push(new google.maps.LatLng(coordList[i].lat, coordList[i].lon));
    }
    options = {
        clickable: false,
        fillColor: '#30A030',
        fillOpacity: 0.3,
        strokeWeight: 0,
        map: map,
        paths: new google.maps.MVCArray(coords)
    }
    var foo = new google.maps.Polygon(options);
}

function drawOffLimitsZones(offLimitsZones) {
    for (var i = 0; i < offLimitsZones.length; i++) {
        drawOffLimitsZone(offLimitsZones[i]);
    }
}

function drawOffLimitsZone(coordList) {
    coords = [];
    for (var i = 0; i < coordList.length; i++) {
        coords.push(new google.maps.LatLng(coordList[i].lat, coordList[i].lon));
    }
    options = {
        clickable: false,
        fillColor: '#FFB300',
        fillOpacity: 0.7,
        strokeColor: '#FF0000',
        map: map,
        paths: new google.maps.MVCArray(coords)
    }
    var foo = new google.maps.Polygon(options);
}

function drawPreviousMovement(coordList){
    var polyOptions = {
        strokeColor: '#FFFFFF',
        strokeOpacity: 0.70,
        strokeWeight: 1.5
    }
    poly = new google.maps.Polyline(polyOptions);
    poly.setMap(map);
    
    for (var i = 0; i < coordList.length; i++) {
        poly.getPath().push(new google.maps.LatLng(coordList[i].lat, coordList[i].lon));
    }
}

function removePreviousMovement(){
    poly.setMap(null);
    poly = null;
}
    

function setFollowingUser(following) {
    comm.setFollowingUser(following);

    if (following) {
        map.setOptions({
            draggable: false,
        });

        if (typeof (locationChangedListener) == 'undefined') {
            locationChangedListener = google.maps.event.addListener(geomarker, "position_changed", function () {
                map.panTo(this.getPosition());
            });
        }
        
        var pos;
        if((pos = geomarker.getPosition()) != null){
            map.panTo(pos);
        }
        lockLocationButton.style.backgroundImage = 'url(./lockLocationOn.png)';
    } else {
        if (typeof (locationChangedListener) != 'undefined') {
            google.maps.event.removeListener(locationChangedListener);
        }
        map.setOptions({
            draggable: true
        });
        locationChangedListener = undefined;
        lockLocationButton.style.backgroundImage = 'url(./lockLocationOff.png)';
    }
}

function lockLocationControl(controlDiv, map) {

  controlDiv.style.margin = '3px';

  lockLocationButton = document.createElement('div');
  lockLocationButton.style.width = '28px';
  lockLocationButton.style.height = '28px';
  lockLocationButton.style.background = '#101010';
  lockLocationButton.style.backgroundPosition = 'center';
  lockLocationButton.style.backgroundRepeat = 'no-repeat';
  lockLocationButton.style.backgroundImage = 'url(./lockLocationOff.png)';
  lockLocationButton.style.backgroundSize = '24px';
  lockLocationButton.style.border = '2px solid #404040';
  lockLocationButton.style.borderRadius = '2px';
  lockLocationButton.innerHTML = '&nbsp;';
  controlDiv.appendChild(lockLocationButton);
  
  zoomInButton = document.createElement('div');
  zoomInButton.style.width = '28px';
  zoomInButton.style.height = '28px';
  zoomInButton.style.background = '#101010';
  zoomInButton.style.backgroundPosition = 'center';
  zoomInButton.style.backgroundRepeat = 'no-repeat';
  zoomInButton.style.backgroundImage = 'url(./zoomIn.png)';
  zoomInButton.style.backgroundSize = '20px';
  zoomInButton.style.border = '2px solid #404040';
  zoomInButton.style.borderRadius = '2px';
  zoomInButton.style.marginTop = '3px';
  zoomInButton.innerHTML = '&nbsp;';
  controlDiv.appendChild(zoomInButton);
  
  zoomOutButton = document.createElement('div');
  zoomOutButton.style.width = '28px';
  zoomOutButton.style.height = '28px';
  zoomOutButton.style.background = '#101010';
  zoomOutButton.style.backgroundPosition = 'center';
  zoomOutButton.style.backgroundRepeat = 'no-repeat';
  zoomOutButton.style.backgroundImage = 'url(./zoomOut.png)';
  zoomOutButton.style.backgroundSize = '18px';
  zoomOutButton.style.border = '2px solid #404040';
  zoomOutButton.style.borderRadius = '2px';
  zoomOutButton.style.marginTop = '3px';
  zoomOutButton.innerHTML = '&nbsp;';
  controlDiv.appendChild(zoomOutButton);

  google.maps.event.addDomListener(lockLocationButton, 'click', function() {
    setFollowingUser(!comm.isFollowingUser());
  });
  
  google.maps.event.addDomListener(zoomInButton, 'click', function() {
    map.setZoom(map.getZoom()+1);
  });
  
  google.maps.event.addDomListener(zoomOutButton, 'click', function() {
    map.setZoom(map.getZoom()-1);
  });
}
