var GuideRoute = [];
var directionsService;
directionsService = new google.maps.DirectionsService();

function calcRoute() {
            var start = "penn station, new york, ny";
            var end = "W 49th St & 5th Ave, New York, NY 10020";
            var request = {
                origin: start,
                destination: end,
                travelMode: google.maps.TravelMode.WALKING
            };
            directionsService.route(request, function(response, status) {
                if (status == google.maps.DirectionsStatus.OK) {
                    showSteps(response);
                }
            });
        }

function showSteps(DirectionResults) {
            var myRoute = DirectionResults.routes[0].legs[0];
            for (var i = 0; i < myRoute.steps.length; i++) {
                var hold =[];
                hold[i] = myRoute.steps[i].instructions;
                if (hold[i].indexOf("left") > -1) {
                    GuideRoute[i] = "L";
                } else if (hold[i].indexOf("Right") > -1) {
                    GuideRoute[i] = "R";
                } else {
                    GuideRoute[i] = "S";
                }
            }
              
