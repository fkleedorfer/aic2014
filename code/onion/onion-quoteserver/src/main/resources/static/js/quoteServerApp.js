var quoteServerApp = angular.module('quoteServerApp', []);

quoteServerApp.controller('QuoteController', function ($scope, $http) {

    $scope.quote = ""; //initialize string
    $scope.isRequesting = false;

    $scope.getQuote = function getQuote(){
        $scope.isRequesting = true;
        $http.get('/quote').
            success(function(data, status, headers, config) {
                $scope.quote = data;
                $scope.isRequesting = false;
            }).
            error(function(data, status, headers, config) {
                $scope.quote = "";
                $scope.isRequesting = false;
            });
    }

});