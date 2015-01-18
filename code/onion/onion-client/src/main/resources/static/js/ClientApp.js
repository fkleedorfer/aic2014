var onionClientApp = angular.module('onionClientApp', []);

onionClientApp.controller('onionClientController', function ($scope, $http) {

    $scope.response = "";

    $scope.sendRequest = function sendRequest(){
               $scope.isRequesting = true;
               $http.get('/sendRequest').
                    success(function(data, status, headers, config) {
                        $scope.response = data;
                        $scope.isRequesting = false;
                    }).
                    error(function(data, status, headers, config) {
                        $scope.response = "error";
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
                   $scope.response = "error";
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
                   $scope.response = "error";
                   $scope.isRequesting = false;
               });
    }

    $scope.sendExit = function sendExit(){
              $scope.isRequesting = true;
              $http.get('/sendExit').
                   success(function(data, status, headers, config) {
                   $scope.response = data;
                   $scope.isRequesting = false;
              }).
              error(function(data, status, headers, config) {
                   $scope.response = "error";
                   $scope.isRequesting = false;
              });
    }

    $scope.sendRequest();
    $scope.sendBomb();
    $scope.sendHelp();
    $scope.sendExit();
});