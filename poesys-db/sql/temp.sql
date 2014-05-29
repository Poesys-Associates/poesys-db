SELECT COUNT(*), SUM(l.timeworked)/3600
  FROM worklog l JOIN
        customfieldvalue cfv ON l.issueid = cfv.issue
 WHERE l.updated BETWEEN '2008-02-11' AND '2008-02-17' AND
       cfv.customfield = 10010 AND
       cfv.stringvalue = 'ABRC';
       
SELECT i.pkey, l.id, SUM(l.timeworked)
FROM jiraissue i JOIN
worklog l ON i.id = l.issueid JOIN
customfieldvalue cfv ON l.issueid = cfv.issue
WHERE cfv.customfield = 10010 AND
cfv.stringvalue = 'ABRC' AND
l.updated BETWEEN '2008-02-11' AND '2008-02-17'
GROUP BY i.pkey, i.id;
