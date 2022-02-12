-- [jooq ignore start]

INSERT INTO page (id, parent, page_url, title, content)
VALUES (1, 'Sponsors', 'benefits', 'Benefits of sponsoring', '<p>The Java User Group Switzerland was founded in 1998. It is the largest software engineering community in Switzerland. It has over 940 individual members, 130 company members and 28 sponsors. JUG Switzerland organizes between 50 and 60 events a year at its locations in Basel, Bern, Lucerne, St. Gallen, Zurich, and YouTube on a wide range of topics relating to Java.</p><p>Our work is largely made possible by the generous support of our sponsors, in return we offer our partners the opportunity to position themselves in the field of Java and Software Engineering.</p><hr><h3>Company Branding</h3><p>Our website, e-mails from JUG (event announcements, discounts, etc.), and slide intros show our sponsors before each event and support the brand awareness of the sponsor with a clearly defined target audience.</p><hr><h3>Java Community Support</h3><p>A sponsorship of the Java User Group supports the Java community and enables the numerous community events, which are highly appreciated and well regarded.</p><hr><h3>Recruiting</h3><p>The Java User Group Switzerland is actively used by the Java Community as a starting point for job searches. The presence as sponsors at the Java User Group Switzerland allows it to directly address initiative employees in the Java ecosystem.</p><hr><h3>Events</h3><p>Our sponsors have the opportunity to suggest topics and speakers for events that are of interest to them. Event proposals should be technical and will be reviewed by the board of JUG Switzerland.</p>'),
       (2, 'Sponsors', 'become-a-sponsor', 'How to become a sponsor', '<h3>How to become a sponsor?</h3><p>We are delighted that you are thinking of becoming a sponsor. Please <a href="mailto:info@jug.ch">contact us</a> and we will get back to you with a copy of our contract with our services and our conditions.</p><hr><h3>Difference between these sponsorships</h3><p>The different sponsorships provide different rights to the sponsors to promote themselves (e.g. through visibility of the logo, newsletter, etc.).<br>A silver sponsorship costs CHF 3000. There is no limit on the number of silver sponsors.<br>A gold sponsorship costs CHF 6000. There can only be max. five gold sponsors at a time.<br>The platinum sponsorship costs CHF 12000. There can only be one platinum sponsor.</p><p>Gold and platinum sponsorships are in high demand, free places will be given to silver sponsors first.</p>');

INSERT INTO redirect (old_url, new_url)
VALUES ('/sponsors.php', '/sponsors'),
       ('/sponsor_benefits.php', '/sponsors/benefits'),
       ('/become_sponsor.php', '/sponsors/become-a-sponsor');

INSERT INTO page (id, parent, page_url, title, content)
VALUES (3, 'Members', 'slack', 'Slack-Channel', '<p>In our <a href="https://jugch.slack.com/" target="_blank">slack channel for members</a> we would like to get into conversation with you.</p><p>Register at <a href="http://slack.jug.ch/" target="_blank">slack.jug.ch</a> and discuss with us and other members!</p>');

INSERT INTO redirect (old_url, new_url)
VALUES ('/members.php', '/members'),
       ('/joinus_slack.php', '/members/slack');

-- [jooq ignore stop]
