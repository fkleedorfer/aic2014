var directoryNodeApp = angular.module('directoryNodeApp', []);

directoryNodeApp.controller('chainNodeInfoController', function ($scope, $http) {

    $scope.chainNodeInfos = []; //initialize to empty array
    $scope.isRequesting = false;

    $scope.reloadChainNodeInfo = function reloadChainNodeInfo(){
        $scope.isRequesting = true;
        $http.get('/chainNode').
            success(function(data, status, headers, config) {
                $scope.chainNodeInfos = data;
                $scope.isRequesting = false;
            }).
            error(function(data, status, headers, config) {
                $scope.chainNodeInfos = [];
                $scope.isRequesting = false;
            });
    }

    $scope.reloadChainNodeInfo();

});