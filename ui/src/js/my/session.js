define(['ojs/ojcore', 'knockout', 'jquery', 'text!data/data.json'],
    function (oj, ko, $, data) {

        function ViewModel() {
            var self = this;

            /* all the interaction between frontend and backend are handled by session.js */
            // self.baseAPI = 'http://' + window.location.hostname + ':8080';
            self.baseAPI = 'http://xxx:8080';
            //self.baseAPI = 'http://localhost:8080';
            self.types = ['html', 'node', 'jar', 'war'];


            self.listApps = function(callback){
                $.ajax({
                    type: "GET",
                    url: self.baseAPI + '/app',
                    data: {},
                    contentType: "application/json",
                    headers: {
                        Accept: "application/json",
                    },
                    success: function (res) {
                        callback(res);
                    }
                });
            }

            self.createOrUpdateApp = function(action, formData, callback){
                $.ajax({
                    url: self.baseAPI + '/' + action,
                    type: 'POST',
                    data: formData,
                    async: true,
                    cache: false,
                    contentType: false,
                    enctype: 'multipart/form-data',
                    processData: false,
                    success: function (res) {
                        callback(res);
                    }
                });
            }


        }

        return new ViewModel();
    }
);
