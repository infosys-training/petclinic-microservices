'use strict';

angular.module('disputeResolution')
    .controller('DisputeDetailController', ['$http', '$stateParams', '$state', function ($http, $stateParams, $state) {
        var self = this;
        self.dispute = null;
        self.loading = true;

        self.loadDispute = function () {
            $http.get('api/genai/disputes/' + $stateParams.disputeId).then(function (resp) {
                self.dispute = resp.data;
                self.loading = false;
            }, function () {
                self.loading = false;
            });
        };

        self.goBack = function () {
            $state.go('disputes');
        };

        self.getAgentIcon = function (agentName) {
            if (agentName.indexOf('Intake') !== -1) return 'fa-sign-in';
            if (agentName.indexOf('Classifier') !== -1) return 'fa-tags';
            if (agentName.indexOf('Fraud') !== -1) return 'fa-exclamation-triangle';
            if (agentName.indexOf('Evidence') !== -1) return 'fa-folder-open';
            if (agentName.indexOf('Decision') !== -1) return 'fa-gavel';
            if (agentName.indexOf('Communication') !== -1) return 'fa-envelope';
            return 'fa-cog';
        };

        self.getAgentColor = function (agentName) {
            if (agentName.indexOf('Intake') !== -1) return '#3498db';
            if (agentName.indexOf('Classifier') !== -1) return '#9b59b6';
            if (agentName.indexOf('Fraud') !== -1) return '#e74c3c';
            if (agentName.indexOf('Evidence') !== -1) return '#f39c12';
            if (agentName.indexOf('Decision') !== -1) return '#2ecc71';
            if (agentName.indexOf('Communication') !== -1) return '#1abc9c';
            return '#95a5a6';
        };

        self.getStatusBadgeClass = function () {
            if (!self.dispute) return '';
            switch (self.dispute.status) {
                case 'APPROVED': return 'bg-success';
                case 'REJECTED': return 'bg-danger';
                case 'ESCALATED': return 'bg-warning text-dark';
                default: return 'bg-info';
            }
        };

        self.getDecisionIcon = function () {
            if (!self.dispute) return '';
            switch (self.dispute.decision) {
                case 'APPROVE': return 'fa-check-circle text-success';
                case 'REJECT': return 'fa-times-circle text-danger';
                case 'ESCALATE': return 'fa-arrow-up text-warning';
                default: return 'fa-question-circle';
            }
        };

        self.loadDispute();
    }]);
