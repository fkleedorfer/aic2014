var clientApp = angular.module('clientApp', []);

clientApp.controller('onionClientAppController', function ($scope, $http) {

    $scope.response = "";
    $scope.isRequesting = false;

    $scope.sendRequest = function sendRequest(){
          $scope.isRequesting = true;
          $http.get('/sendRequest').
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
        $http.get('/sendBomb').
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