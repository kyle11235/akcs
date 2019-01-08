/**
 * Copyright (c) 2014, 2017, Oracle and/or its affiliates.
 * The Universal Permissive License (UPL), Version 1.0
 */
/*
 * Your application specific code will go here
 */
define(['ojs/ojcore', 'knockout', 'ojs/ojrouter', 'ojs/ojknockout', 'ojs/ojarraytabledatasource',
  'ojs/ojoffcanvas'],
  function(oj, ko) {
     function ControllerViewModel() {
       var self = this;

      // Media queries for repsonsive layouts
      var smQuery = oj.ResponsiveUtils.getFrameworkQuery(oj.ResponsiveUtils.FRAMEWORK_QUERY_KEY.SM_ONLY);
      self.smScreen = oj.ResponsiveKnockoutUtils.createMediaQueryObservable(smQuery);
      var mdQuery = oj.ResponsiveUtils.getFrameworkQuery(oj.ResponsiveUtils.FRAMEWORK_QUERY_KEY.MD_UP);
      self.mdScreen = oj.ResponsiveKnockoutUtils.createMediaQueryObservable(mdQuery);

       // Router setup
       self.router = oj.Router.rootInstance;
       self.router.configure({
         'home': {label: 'Home', isDefault: true}}
         );
       oj.Router.defaults['urlAdapter'] = new oj.Router.urlParamAdapter();





      // Header
      // Application Name used in Branding Area
      self.appName = ko.observable("CLOUD My Services");
      // User Info used in Global Navigation area
      self.userLogin = ko.observable("xxx@xxx.com");

      // Footer
      function footerLink(name, id, linkTarget) {
        this.name = name;
        this.linkId = id;
        this.linkTarget = linkTarget;
      }
      self.footerLinks = ko.observableArray([
        new footerLink('About', 'about', 'http://www.xxx.com/'),
        new footerLink('Contact Us', 'contactUs', 'http://www.xxx.com'),
        new footerLink('Legal Notices', 'legalNotices', 'http://www.xxx.com'),
        new footerLink('Terms Of Use', 'termsOfUse', 'http://www.xxx.com'),
        new footerLink('Your Privacy Rights', 'yourPrivacyRights', 'http://www.xxx.com')
      ]);
     }

     return new ControllerViewModel();
  }
);
