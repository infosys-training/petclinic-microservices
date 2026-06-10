'use strict';

angular.module('disputeResolution', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('disputes', {
                parent: 'app',
                url: '/disputes',
                template: '<dispute-list></dispute-list>'
            })
            .state('disputeDetail', {
                parent: 'app',
                url: '/disputes/:disputeId',
                template: '<dispute-detail></dispute-detail>'
            })
    }]);
