CREATE OR REPLACE VIEW retrieved_documents_view AS
SELECT rd.docid, rd.doctype, rd.rank, a.content
FROM retrieved_documents rd
         LEFT JOIN argument a on a.argid = rd.docid
WHERE rd.doctype = 'argument'

UNION

SELECT rd.docid, rd.doctype, rd.rank, p.title
FROM retrieved_documents rd
         LEFT JOIN premise p on p.pid = rd.docid
WHERE rd.doctype = 'premise'

UNION

SELECT rd.docid, rd.doctype, rd.rank, d.title
FROM retrieved_documents rd
         LEFT JOIN discussion d on d.did = rd.docid
WHERE rd.doctype = 'discussion';


CREATE OR REPLACE VIEW discussion_view AS
SELECT d.did     discussion_id,
       d.title   discussion_title,
       p.pid     premise_id,
       p.title   premise_title,
       a.argid   argument_id,
       a.content argument_content,
       a.ispro argument_is_pro
FROM discussion d
         LEFT JOIN premise p on d.did = p.did
         LEFT JOIN argument a on p.pid = a.pid;
