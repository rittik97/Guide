/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

// this is MetaWear's UART service
var metawear = {
    serviceUUID: "326a9000-85cb-9195-d9dd-464cfbbae75a",
    txCharacteristic: "326a9001-85cb-9195-d9dd-464cfbbae75a", // transmit is from the phone's perspective
    rxCharacteristic: "326a9006-85cb-9195-d9dd-464cfbbae75a"  // receive is from the phone's perspective
};

var app = {

    deviceId: ""

    // Application Constructor
    initialize: function () {
        this.bindEvents();
    },
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function () {
        document.addEventListener('deviceready', this.onDeviceReady, false);
        refreshButton.addEventListener('touchstart', this.refreshDeviceList, false);
        motorButton.addEventListener('click', this.onMotorButton, false);
        deviceList.addEventListener('touchstart', this.connect, false);
    },

    // deviceready Event Handler
    OnDeviceReady: function () {
    //Initialise Geolocation and Google Directions Service. 
        navigator.geolocation.getCurrentPosition(app.onSuccess, app.onError)
        var GuideRoute;
        var directionsService;
        GuideRoute = [];
        directionsService = new google.maps.DirectionsService()
    //Refresh Bluetooth Device List and scan for metaWear
        app.refreshDeviceList();
    // Start metaWear
        metawearStart()
    },

    onSuccess: function (position) {
        var Longitude = position.coords.longitude;
        var Latitude = position.coords.latitude;
        var latlong = new google.maps.LatLng(Latitude, Longitude);
        var Origin;
        Origin = latlong;
    },

    refreshDeviceList: function(){
        deviceList.innerHTML = ''; // empties the list
        if (cordova.platformId === 'android') { // Android filtering is broken
            ble.scan([], 5, app.onDiscoverDevice, app.onError);
        } else {
            ble.scan([metawear.serviceUUID], 5, app.onDiscoverDevice, app.onError);
        }
    },

    onDiscoverDevice: function(device) {
        var listItem = document.createElement('li'),
            html = '<b>' + device.name + '</b><br/>' +
                'RSSI: ' + device.rssi + '&nbsp;|&nbsp;' +
                device.id;
    listItem.dataset.deviceId = device.id;
    listItem.innerHTML = html;
    deviceList.appendChild(listItem);
    },

    connect: function(e) {
        app.deviceId = e.target.dataset.deviceId;
        var onConnect = function() {
            app.enableButtonFeedback(app.subscribeForIncomingData, app.onError);
            app.showDetailPage();
        };
        ble.connect(app.deviceId, onConnect, app.onError);
    },

    writeData: function(buffer, success, failure) { // to to be sent to MetaWear
        if (!success) {
            success = function() {
                console.log("success");
                resultDiv.innerHTML = resultDiv.innerHTML + "Sent: " + JSON.stringify(new Uint8Array(buffer)) + "<br/>";
                resultDiv.scrollTop = resultDiv.scrollHeight;
            };
        }
        if (!failure) {
            failure = app.onError;
        }
        ble.writeCommand(app.deviceId, metawear.serviceUUID, metawear.txCharacteristic, buffer, success, failure);
    },

    onMotorButton: function(event) {
        var pulseWidth = pulseWidthInput.value;
        var data = new Uint8Array(6);
        data[0] = 0x07; // module
        data[1] = 0x01; // pulse ops code
        data[2] = 0x80; // Motor
        data[3] = pulseWidth & 0xFF; // Pulse Width
        data[4] = pulseWidth >> 8; // Pulse Width
        data[5] = 0x00; // Some magic bullshit

        app.writeData(data.buffer);
    },
    // Update DOM on a Received Event

};

app.initialize();
