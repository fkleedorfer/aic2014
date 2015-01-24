var clientApp = angular.module('clientApp', []);

clientApp.controller('onionClientAppController', function ($scope, $http) {

    $scope.response = "";
    $scope.isRequesting = false;

    $scope.sendRequest = function sendRequest(){
          $scope.isRequesting = true;
          $http.get('/sendRequest').
               success(function(data, status, headers, config) {
                    $scope.response = data;
                    $scope.isRequesting = false;
               }).
               error(function(data, status, headers, config) {
                    $scope.response = data;
                    $scope.isRequesting = false;
               });
    }

    $scope.sendBomb = function sendBomb(){
        $scope.isRequesting = true;
        $http.get('/sendBomb').
            success(function(data, status, headers, config) {
                $scope.response = data;
                $scope.isRequesting = false;
            }).
            error(function(data, status, headers, config) {
                $scope.response = data;
                $scope.isRequesting = false;
        });
    }

    $scope.sendHelp = function sendHelp(){
          $scope.isRequesting = true;
          $http.get('/sendHelp').
               success(function(data, status, headers, config) {
                   $scope.response = data;
                   $scope.isRequesting = false;
               }).
               error(function(data, status, headers, config) {
                   $scope.response = data;
                   $scope.isRequesting = false;
               });
    }

    $scope.sendExit = function sendExit(){
              $scope.response = "Client stopped...";
              $scope.isRequesting = true;
              $http.get('/sendExit').
                   success(function(data, status, headers, config) {
                   $scope.isRequesting = false;
              }).
              error(function(data, status, headers, config) {
                   $scope.isRequesting = false;
              });
    }

});