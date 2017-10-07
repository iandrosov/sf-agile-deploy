# sf-agile-deploy
## Salesforce Agile Deployment Tool

This is deployment tool work in progress to simplify Salesforce deployments. This app provide services that enables users of [Salesforce Agile Accelerator](https://appexchange.salesforce.com/listingDetail?listingId=a0N30000000ps3jEAA) to deploy stories direct with a button-click. It enables developers and admins alike to use source driven develpment with github.

The app is built with Java Spring-Boot to easy integration with ant and jgit libraries and REST API to enable metadata deployments. The app is intended to host on [Heroku](https://www.heroku.com/platform) platform, and can be run on local server or other containers.

The app provides REST API services to enable deployments of Salesforce metadata using Github repository.

Dependencies

+ [Salesforce Agile Accelerator](https://appexchange.salesforce.com/listingDetail?listingId=a0N30000000ps3jEAA) - Agile Story Manager tool
+ [sf-agile-deploy](https://github.com/iandrosov/sf-agile-deploy) - REST Services on Heroku
+ [ANT Migration tool](https://developer.salesforce.com/page/Force.com_Migration_Tool)
+ [JGit](https://www.eclipse.org/jgit/)


