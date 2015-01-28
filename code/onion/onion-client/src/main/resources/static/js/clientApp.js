var clientApp = angular.module('clientApp', []);

clientApp.controller('onionClientAppController', function ($scope, $http) {

    $scope.response = "";
    $scope.inputUri = "";
    $scope.isRequesting = false;
    $scope.options = [{ "name": "1" }, { "name": "2" }, { "name": "3" }, { "name": "4" }, { "name": "5" }, { "name": "6" },
     { "name": "7" }, { "name": "8" }, { "name": "9" }, { "name": "10" }, { "name": "11" }, { "name": "12" },
     { "name": "13" }, { "name": "14" }, { "name": "15" }, { "name": "16" }, { "name": "17" }, { "name": "18" },
     { "name": "19" }, { "name": "20" }];

    $scope.sendRequest = function sendRequest(){
          $scope.isRequesting = true;
          //$http.get('/sendRequest').
          $http.put('/sendRequest?url=' + $scope.inputUri).
               success(function(data, status, headers, config) {
                    $scope.response = data.text + $scope.response;
                    $scope.isRequesting = false;
                    makeLog();
               }).
               error(function(data, status, headers, config) {
                    $scope.response = data.text + $scope.response;
                    $scope.isRequesting = false;
                    makeLog();
               });
    }

    $scope.sendBomb = function sendBomb(){
        $scope.isRequesting = true;
        $http.put('/sendBomb?n=' + $scope.optionList.name + '&url=' + $scope.inputUri).
        //$http.get('/sendBomb').
            success(function(data, status, headers, config) {
                    $scope.response = data.text + $scope.response;
                    $scope.isRequesting = false;
            }).
            error(function(data, status, headers, config) {
                    $scope.response = data.text + $scope.response;
                    $scope.isRequesting = false;
        });
    }

    $scope.sendHelp = function sendHelp(){
          $scope.isRequesting = true;
          $http.get('/sendHelp').
               success(function(data, status, headers, config) {
                    $scope.response = data.text + $scope.response;
                    $scope.isRequesting = false;
               }).
               error(function(data, status, headers, config) {
                    $scope.response = data.text + $scope.response;
                    $scope.isRequesting = false;
               });
    }

    $scope.sendExit = function sendExit(){
              $scope.isRequesting = true;
              $scope.response = "Client has stopped!" +
                                "\n--------------------------------------------------------------------------------------------\n" +
                                $scope.response;
              $http.get('/sendExit').
                   success(function(data, status, headers, config) {
                   $scope.response = data.text + $scope.response;
                   $scope.isRequesting = false;
              }).
              error(function(data, status, headers, config) {
                   $scope.response = data.text + $scope.response;
                   $scope.isRequesting = false;
              });
    }
});