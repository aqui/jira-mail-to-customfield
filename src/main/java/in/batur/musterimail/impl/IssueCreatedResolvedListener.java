package in.batur.musterimail.impl;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.CustomField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Component
public class IssueCreatedResolvedListener implements InitializingBean, DisposableBean 
{
    static final String FROM = "";
    static final String FROMNAME = "";
    static final String TO = "";
    static final String SMTP_USERNAME = "";
    static final String SMTP_PASSWORD = "";
    static final String CONFIGSET = "ConfigSet";
    static final String HOST = "";
    static final int PORT = 587;
    
    private static final Logger log = LoggerFactory.getLogger(IssueCreatedResolvedListener.class);

    @JiraImport
    private final EventPublisher eventPublisher;

    @Autowired
    public IssueCreatedResolvedListener(EventPublisher eventPublisher) 
    {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Called when the plugin has been enabled.
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception 
    {
        log.info("Enabling plugin");
        eventPublisher.register(this);
    }

    /**
     * Called when the plugin is being disabled or removed.
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception 
    {
        log.info("Disabling plugin");
        eventPublisher.unregister(this);
    }

    @EventListener
    public void onIssueEvent(IssueEvent issueEvent)
    {
        Long eventTypeId = issueEvent.getEventTypeId();
        Issue issue = issueEvent.getIssue();
        CustomField cf1 = ComponentAccessor.getCustomFieldManager().getCustomFieldObject((long) 11300); //11300
        CustomField cf2 = ComponentAccessor.getCustomFieldManager().getCustomFieldObject((long) 11400); //11400
        System.out.println(issue.getStatusId());
        //Issue kapatıldıysa ve müşteri mail'i girildiyse
        if(issue.getStatusId().equals("6") && cf1.getValueFromIssue(issue) != null)
        {
            System.out.println("======================================================");
            System.out.println("Status ID: "+issue.getStatusId());
            System.out.println("Issue Key: "+issue.getKey());
            System.out.println("Issue Description: "+issue.getDescription());
            System.out.println("Issue Summary: "+issue.getSummary());
            System.out.println("Customer Mail: "+cf1.getValueFromIssue(issue));
            System.out.println("Customer Info: "+cf2.getValueFromIssue(issue));
            String musteriMail = cf1.getValueFromIssue(issue);
            String bilgilendirmeNotu;
            if(cf2.getValueFromIssue(issue) == null)
            {
                bilgilendirmeNotu = "";
            }
            else
            {
                bilgilendirmeNotu = cf2.getValueFromIssue(issue);
            }
            String description;
            if(issue.getDescription() == null)
            {
                description = "";
            }
            else
            {
                description = issue.getDescription();
            }
            String subject = "Issue is closed: "+issue.getKey();
            String body = String.join(System.getProperty("line.separator"),"<b>Hi,</b>","<p>The issue is closed.</p>","<p><b>"+issue.getKey()+":</b> "+issue.getSummary()+"</p>","<p><b>Description:</b></p>","<p>"+description+"</p>","<p><b>Note: </b><p>"+bilgilendirmeNotu+"</p></p>","<p>Bilginize sunarız.</p>");
            String BODY = replaceTurkce(body);
            String SUBJECT = replaceTurkce(subject);
            System.out.println("Subject: "+subject);
            System.out.println("Body: "+body);
            System.out.println("======================================================");
            //sendMail(musteriMail, subject, body);
            try
            {
                String[] parts = musteriMail.split(",");
                for(String mail : parts)
                {
                    Properties props = System.getProperties();
                    props.put("mail.transport.protocol", "smtp");
                    props.put("mail.smtp.port", PORT); 
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.auth", "true");
                    Session session = Session.getDefaultInstance(props);
                    MimeMessage msg = new MimeMessage(session);
                    msg.setFrom(new InternetAddress(FROM,FROMNAME));
                    msg.setRecipient(Message.RecipientType.TO, new InternetAddress(mail.trim()));
                    msg.setSubject(SUBJECT);
                    msg.setContent(BODY,"text/html; charset=UTF-8");
                    msg.setHeader("X-SES-CONFIGURATION-SET", CONFIGSET);
                    Transport transport = session.getTransport();
                    try
                    {
                        System.out.println("Sending...");
                        // Connect to Amazon SES using the SMTP username and password you specified above.
                        transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);
                        // Send the email.
                        transport.sendMessage(msg, msg.getAllRecipients());
                        System.out.println("Email sent: "+mail);
                    }
                    catch (Exception ex) 
                    {
                        System.out.println("The email was not sent.");
                        System.out.println("Error message: " + ex.getMessage());
                    }
                    finally
                    {
                        // Close and terminate the connection.
                        transport.close();
                    }
                }
                
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public void sendMail(String musteriMail, String subject, String body)
    {
        String s = null;
        try 
        {
            String[] cmd = 
            {
                "/bin/sh",
                "-c",
                "echo \""+body+"\" | mailx -v -r \"\" -s \""+subject+"\" -S smtp=\":587\" -S smtp-use-starttls -S smtp-auth=login -S smtp-auth-user=\"\" -S smtp-auth-password=\"\" -S ssl-verify=ignore "+musteriMail.trim()
            };
            Process process = Runtime.getRuntime().exec(cmd);
            // run the Unix "ps -ef" command
            // using the Runtime exec method:
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) 
            {
                System.out.println(s);
            }
            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) 
            {
                System.out.println(s);
            }
            int exitCode = 0;
            try
            {
                exitCode = process.waitFor();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            System.out.println("exitKod = " + exitCode);
        }
        catch (IOException e) 
        {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
        }
    }
    
    public String replaceTurkce(String turkce)
    {
        turkce.replaceAll("ç", "c");
        turkce.replaceAll("Ç", "C");
        turkce.replaceAll("ö", "o");
        turkce.replaceAll("Ö", "O");
        turkce.replaceAll("ü", "u");
        turkce.replaceAll("Ü", "U");
        turkce.replaceAll("İ", "I");
        turkce.replaceAll("ı", "i");
        turkce.replaceAll("Ğ", "G");
        turkce.replaceAll("ğ", "g");
        turkce.replaceAll("Ş", "S");
        turkce.replaceAll("ş", "s");
        return turkce;
    }
}
