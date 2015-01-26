var chainNodeApp = angular.module('chainNodeApp', []);

chainNodeApp.controller('routingInfoController', function ($scope, $http, $interval) {

    $scope.routingInfo = []; //initialize to empty array
    $scope.stats = {};
    $scope.isRequesting = false;
    $scope.errorSimulationMode = 'NO_ERROR';

    $scope.reloadRoutingInfo = function reloadRoutingInfo(){
        $scope.isRequesting = true;
        $http.get('/routingInfo').
            success(function(data, status, headers, config) {
                $scope.routingInfo = data;
                $scope.isRequesting = false;
            }).
            error(function(data, status, headers, config) {
                $scope.routingInfo = [];
                $scope.isRequesting = false;
            });
    }

    $scope.setErrSim = function setErrSim(){
        $scope.isRequesting = true;
        $http.put('/errSim?mode=' + $scope.errorSimulationMode).
            success(function(data, status, headers, config) {
                //we set the local value to that received from the server,
                //just to be sure we have the server's value.
                $scope.errorSimulationMode = data;
                $scope.isRequesting = false;
            }).
            error(function(data, status, headers, config) {
                $scope.isRequesting = false;
            });
    }

    $scope.getErrSim = function getErrSim(){
        $scope.isRequesting = true;
        $http.get('/errSim').
            success(function(data, status, headers, config) {
                $scope.errorSimulationMode = data;
                $scope.isRequesting = false;
            }).
            error(function(data, status, headers, config) {
                $scope.isRequesting = false;
            });
    }

    $scope.getStats = function getStats(){
        $scope.isRequesting = true;
        $http.get('/ping' ).
            success(function(data, status, headers, config) {
                $scope.stats = data;
                $scope.isRequesting = false;
            }).
            error(function(data, status, headers, config) {
                $scope.isRequesting = false;
            });
    }

    $scope.getStats();
    $scope.reloadRoutingInfo();
    $scope.getErrSim();
    //load every 3 seconds
    $interval($scope.getStats,3000);
    //load on startup
    $scope.getStats();

});