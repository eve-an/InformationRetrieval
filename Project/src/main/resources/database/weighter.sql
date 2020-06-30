WITH occ(tid, aocc, pocc, docc) AS 
(
    SELECT tid, MAX(A.aocc), MAX(A.pocc), MAX(A.docc)
    FROM
    (   
        
            
        SELECT tid, COUNT(*) as aocc, 0 as pocc, 0 as docc
        FROM argument_index
        GROUP BY tid
            
        UNION ALL
            
        SELECT tid, 0 as aocc, COUNT(*) as pocc, 0 as docc
        FROM premise_index
        GROUP BY tid
            
        UNION ALL
        
        SELECT tid, 0 as aocc, 0 as pocc, COUNT(*) as docc
        FROM discussion_index
        GROUP BY tid
        
    ) AS A
    GROUP BY tid
),
argumentcount(acount) AS (SELECT COUNT(*) as acount FROM argument),
premisecount(pcount) AS (SELECT COUNT(*) as pcount FROM premise),
discussioncount(dcount) AS (SELECT COUNT(*) as dcount FROM discussion),
arg_up AS (UPDATE argument_index SET weight = ((1 + ln(occurrences)) * ln((SELECT acount::DECIMAL FROM argumentcount)/(SELECT aocc FROM occ O WHERE O.tid=argument_index.tid)))),
pid_up AS (UPDATE premise_index SET weight = ((1 + ln(occurrences)) * ln((SELECT pcount::DECIMAL FROM premisecount)/(SELECT pocc FROM occ O WHERE O.tid=premise_index.tid)))),
did_up AS (UPDATE discussion_index SET weight = ((1 + ln(occurrences)) * ln((SELECT dcount::DECIMAL FROM discussioncount)/(SELECT docc FROM occ O WHERE O.tid=discussion_index.tid))))
SELECT 'Updated weight' AS Result FROM argumentcount;