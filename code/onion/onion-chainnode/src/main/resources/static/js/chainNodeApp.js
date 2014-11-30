var chainNodeApp = angular.module('chainNodeApp', []);

chainNodeApp.controller('routingInfoController', function ($scope, $http) {

    $scope.routingInfo = []; //initialize to empty array
    $scope.isRequesting = false;

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

    $scope.reloadRoutingInfo();

});