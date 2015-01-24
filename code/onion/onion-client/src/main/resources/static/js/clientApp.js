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
                    makeLog();
            }).
            error(function(data, status, headers, config) {
                    $scope.response = data.text + $scope.response;
                    $scope.isRequesting = false;
                    makeLog();
        });
    }

    $scope.sendHelp = function sendHelp(){
          $scope.isRequesting = true;
          $http.get('/sendHelp').
               success(function(data, status, headers, config) {
                    $scope.response = data.text + $scope.response;
                    $scope.isRequesting = false;
                    $('#textview').scrollTop($('#textview')[0].scrollHeight);
               }).
               error(function(data, status, headers, config) {
                    $scope.response = data.text + $scope.response;
                    $scope.isRequesting = false;
                    $('#textview').scrollTop($('#textview')[0].scrollHeight);
               });
    }

    $scope.sendExit = function sendExit(){
              $scope.isRequesting = true;
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