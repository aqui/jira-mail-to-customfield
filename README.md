# jira-mail-to-customfield

You can use this plugin to send mail when there is an event occurs on an issue.

It checks for the status ID of the issue. You can change it as you wish.

//http://localhost:8080/rest/api/2/status
//6 is mean "closed"
issue.getStatusId().equals("6")

Go to your Jira: http://localhost:8080/rest/api/2/field

Find the filed that you want to use and copy ID of the field.

In IssueCreatedResolvedListener.java, 

//field ID is 11300. This one is used to get mail addresses.
CustomField cf1 = ComponentAccessor.getCustomFieldManager().getCustomFieldObject((long) 11300);
//field ID is 11400. This one is used for an extra information about the issue.
CustomField cf2 = ComponentAccessor.getCustomFieldManager().getCustomFieldObject((long) 11400);

Note: You can ignore sendMail and replaceTurkce methods. It's all happening in onIssueEvent method.