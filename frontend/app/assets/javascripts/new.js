var duelGGApp = angular.module('yay', ['ui.bootstrap', "ui.bootstrap.tpls", "ngSanitize", "angularFileUpload", ]);

duelGGApp.controller('Root', function($scope, $http) {
    $scope.currentStyle = {
        id : "monokai"
    };
});

duelGGApp.controller('Sygments', function($scope, $http, $fileUploader) {


});
