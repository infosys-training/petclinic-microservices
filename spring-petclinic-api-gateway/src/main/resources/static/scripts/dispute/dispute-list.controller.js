'use strict';

angular.module('disputeResolution')
    .controller('DisputeListController', ['$http', '$state', function ($http, $state) {
        var self = this;
        self.disputes = [];
        self.showForm = false;
        self.submitting = false;
        self.errorMessage = '';

        self.newDispute = {
            customerName: '',
            customerEmail: '',
            transactionId: '',
            amount: '',
            merchantName: '',
            transactionDate: '',
            cardLast4: '',
            channel: 'internet_banking',
            issueDescription: ''
        };

        self.loadDisputes = function () {
            $http.get('api/genai/disputes').then(function (resp) {
                self.disputes = resp.data;
            });
        };

        self.toggleForm = function () {
            self.showForm = !self.showForm;
            self.errorMessage = '';
        };

        self.submitDispute = function () {
            if (!self.newDispute.customerName || !self.newDispute.transactionId ||
                !self.newDispute.amount || !self.newDispute.issueDescription) {
                self.errorMessage = 'Please fill in all required fields.';
                return;
            }

            self.submitting = true;
            self.errorMessage = '';

            var payload = angular.copy(self.newDispute);
            payload.amount = parseFloat(payload.amount);

            $http.post('api/genai/disputes', payload).then(function (resp) {
                self.submitting = false;
                self.showForm = false;
                self.loadDisputes();
                self.resetForm();
            }, function (err) {
                self.submitting = false;
                self.errorMessage = 'Failed to submit dispute. Please try again.';
            });
        };

        self.viewDetail = function (disputeId) {
            $state.go('disputeDetail', { disputeId: disputeId });
        };

        self.getStatusClass = function (status) {
            switch (status) {
                case 'APPROVED': return 'badge bg-success';
                case 'REJECTED': return 'badge bg-danger';
                case 'ESCALATED': return 'badge bg-warning text-dark';
                default: return 'badge bg-info';
            }
        };

        self.resetForm = function () {
            self.newDispute = {
                customerName: '',
                customerEmail: '',
                transactionId: '',
                amount: '',
                merchantName: '',
                transactionDate: '',
                cardLast4: '',
                channel: 'internet_banking',
                issueDescription: ''
            };
        };

        self.loadDisputes();
    }]);
