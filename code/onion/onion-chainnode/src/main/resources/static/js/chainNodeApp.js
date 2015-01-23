var chainNodeApp = angular.module('chainNodeApp', []);

chainNodeApp.controller('routingInfoController', function ($scope, $http) {

    $scope.routingInfo = []; //initialize to empty array
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

    $scope.reloadRoutingInfo();
    $scope.getErrSim();

});