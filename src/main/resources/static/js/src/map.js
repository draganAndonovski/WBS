function initMap() {
    var geocoder = new google.maps.Geocoder();
    var infowindow = new google.maps.InfoWindow;
    var city = "Skopje";
    var myOptions = {
        center: new google.maps.LatLng(41.999069, 21.3548497),
        zoom: 12,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    var map = new google.maps.Map(document.getElementById("map_canvas"),
        myOptions);

    function geocodeAddress(geocoder, resultsMap) {
        var address = document.getElementById('address').value;
        geocoder.geocode({'address': address}, function (results, status) {
            if (status === 'OK') {
                city = address;
                resultsMap.setCenter(results[0].geometry.location);
                var marker = new google.maps.Marker({
                    map: resultsMap,
                    position: results[0].geometry.location
                });
                sendCity()
            } else {
                alert('Geocode was not successful for the following reason: ' + status);
            }
        });
    }

    document.getElementById('submit').addEventListener('click', function () {
        geocodeAddress(geocoder, map);
    });

    google.maps.event.addListener(map, 'click', function (event) {

        geocoder.geocode({'latLng': event.latLng}, function (results, status) {
            if (status == google.maps.GeocoderStatus.OK) {
                if (results[1]) {
                    map.setZoom(11);
                    marker = new google.maps.Marker({
                        position: event.latLng,
                        map: map
                    });
                    if (results.length > 8) {
                        city = results[1].address_components[2].long_name;
                        console.log(city);
                    }
                    if (results.length < 5) {
                        city = results[1].address_components[0].long_name;
                        console.log(city);
                    }
                    else {
                        city = results[1].address_components[1].long_name;
                        console.log(city);
                    }
                    infowindow.setContent(results[1].formatted_address);
                    infowindow.open(map, marker);
                    sendCity()
                }
            } else {
                alert("Geocoder failed due to: " + status);
            }
        });
    });

    function sendCity() {
        $.post('/city', {city: city}, function (returnedData) {
            console.log(returnedData);
            window.location = "/city";
        });
    }

}