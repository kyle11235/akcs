/**
 * Copyright (c) 2014, 2017, Oracle and/or its affiliates.
 * The Universal Permissive License (UPL), Version 1.0
 */
/*
 * Your customer ViewModel code goes here
 */
define(['ojs/ojcore', 'knockout', 'jquery', 'my/session', 'ojs/ojknockout', 'ojs/ojinputtext',
        'ojs/ojbutton', 'promise', 'ojs/ojtable', 'ojs/ojgauge', 'ojs/ojarraytabledatasource',
        'ojs/ojpopup', 'ojs/ojselectcombobox'
    ],
    function (oj, ko, $, session) {
        function ViewModel() {

            var self = this;
            self.session = session;
            self.status = ko.observable('');
            self.popupIndex = ko.observable(1);
            self.close = function () {
                document.querySelector('#popup').close();
            }
            self.open = function () {
                document.querySelector('#popup').open();
            }
            self.showMe = function (index) {
                return index === self.popupIndex();
            }

            // listApps
            self.observableArray = ko.observableArray([]);
            self.datasource = new oj.ArrayTableDataSource(self.observableArray, {idAttribute: 'name'});
            self.columnArray = [
                {
                    "headerText": "Name",
                    "field": "name",
                    "sortable": "enabled",
                    "sortProperty": "name"
                },
                {
                    "headerText": "Type",
                    "renderer": oj.KnockoutTemplateUtils.getRenderer("type", true),
                },
                {
                    "headerText": "URL",
                    "renderer": oj.KnockoutTemplateUtils.getRenderer("url", true),
                },
                {
                    "headerRenderer": oj.KnockoutTemplateUtils.getRenderer("oracle_link_header", true),
                    "renderer": oj.KnockoutTemplateUtils.getRenderer("oracle_link", true)
                }]


            self.listApps = function () {
                self.status('(Refreshing ... )');
                session.listApps(function (data) {
                    console.log(JSON.stringify(data));
                    self.observableArray(data);
                    self.datasource = new oj.ArrayTableDataSource(self.observableArray, {idAttribute: 'name'});
                    self.status('');
                });
            }

            // create or update App
            self.action = ko.observable('Create');
            self.actioning = ko.observable(false);
            self.name = ko.observable('');
            self.placeholder = ko.observable('app name');
            self.warning = ko.observable('');
            self.disabled = ko.computed(function () {
                if (!self.name()) {
                    return true;
                }
                let dn = /^(?![0-9]+$)(?!-)[a-zA-Z0-9%-]{1,63}(?!-)$/;
                if (!dn.test(self.name())) {
                    self.warning('invalid name (e.g. hello-123)');
                    return true;
                }
                if (self.actioning()) {
                    return true;
                }
                self.warning('');
                return false;
            }, self);


            self.type = ko.observable('');
            self.create = function () {
                var popup = document.querySelector('#popup');
                popup.position = {
                    'my': {
                        'horizontal': 'end',
                        'vertical': 'top'
                    },
                    'at': {
                        'horizontal': 'right',
                        'vertical': 'bottom'
                    },
                    'collision': 'none'
                };
                self.action('Create');
                self.name('');
                self.type(session.types[0]); // default type
                self.popupIndex(1);
                self.open();
            }
            window.update = function (name, type) {
                var popup = document.querySelector('#popup');
                popup.position = {
                    'my': {
                        'horizontal': 'center',
                        'vertical': 'center'
                    },
                    'at': {
                        'horizontal': 'center',
                        'vertical': 'center'
                    },
                    'collision': 'none'
                };
                self.action('Update');
                self.name(name);
                self.type(type);
                self.popupIndex(1);
                self.open();
            }

            self.res = ko.observable('');
            self.tips = ko.observable('');
            self.submit1 = function () {
                self.tips('');
                self.actioning(true);
                self.status('(' + self.action() + ' ...)');
                var formData = new FormData($('#form')[0]);
                session.createOrUpdateApp(self.action().toLowerCase(), formData, function (res) {
                    self.actioning(false);
                    self.res(res);
                    if (res === 'success') {
                        self.tips('(it may takes several seconds to be fully running after URL is ready)');
                    }
                    self.popupIndex(2);
                    self.status('');
                    self.listApps();
                });
            }

            self.handleActivated = function () {
                self.actioning(false);
                self.listApps();
            };

        }

        return new ViewModel();
    }
);
