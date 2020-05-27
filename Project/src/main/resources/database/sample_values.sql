
DO $$
DECLARE 
    helloTokenId int;
    worldTokenId int;
    thisTokenId int;
    isTokenId int;
    aTokenId int;
    testTokenId int;

    debateOrgSourceId int;
    wikipediaOrgSourceId int;
    discussionDeSourceId int;

    discussion1Id int;
    discussion2Id int;
    discussion3Id int;
    discussion4Id int;
    discussion5Id int;

    premise1Id int;
    premise2Id int;
    premise3Id int;
    premise4Id int;
    premise5Id int;
    premise6Id int;
    premise7Id int;
    premise8Id int;
    premise9Id int;
    premise10Id int;

    argument1Id int;
    argument2Id int;
    argument3Id int;
    argument4Id int;
    argument5Id int;
    argument6Id int;
    argument7Id int;
    argument8Id int;
    argument9Id int;
    argument10Id int;
BEGIN
    --INSERT INTO token(token) VALUES ('hello') RETURNING tID INTO helloTokenId;
    --INSERT INTO token(token) VALUES ('world') RETURNING tID INTO worldTokenId;
    --INSERT INTO token(token) VALUES ('this') RETURNING tID INTO thisTokenId;
    --INSERT INTO token(token) VALUES ('is') RETURNING tID INTO isTokenId;
    --INSERT INTO token(token) VALUES ('a') RETURNING tID INTO aTokenId;
    --INSERT INTO token(token) VALUES ('test') RETURNING tID INTO testTokenId;

    INSERT INTO source(domain) VALUES ('debate.org') RETURNING sourceID INTO debateOrgSourceId;
    --INSERT INTO source(domain) VALUES ('wikipedia.org') RETURNING sourceID INTO wikipediaOrgSourceId;
    --INSERT INTO source(domain) VALUES ('discussion.de') RETURNING sourceID INTO discussionDeSourceId;

    INSERT INTO discussion(sourceID, crawlID, title, url) VALUES (debateOrgSourceId, 'dis1', 'Discussion test', 'debate.org/discussion1') RETURNING dID INTO discussion1Id;
    --INSERT INTO discussion(sourceID, crawlID, title, url) VALUES (debateOrgSourceId, 'dis2', 'Discussion istzz', 'debate.org/discussion2') RETURNING dID INTO discussion2Id;
    --INSERT INTO discussion(sourceID, crawlID, title, url) VALUES (wikipediaOrgSourceId, 'dis3', 'Discussion freedom', 'wikipedia.org/discussion3') RETURNING dID INTO discussion3Id;
    --INSERT INTO discussion(sourceID, crawlID, title, url) VALUES (wikipediaOrgSourceId, 'dis4', 'Discussion cossur', 'wikipedia.org/discussion4') RETURNING dID INTO discussion4Id;
    --INSERT INTO discussion(sourceID, crawlID, title, url) VALUES (discussionDeSourceId, 'dis5', 'Discussion hello', 'discussion.de/discussion5') RETURNING dID INTO discussion5Id;

    INSERT INTO premise(dID, crawlID, title) VALUES (discussion1Id, 'prem1', 'this is one premise') RETURNING pID INTO premise1Id;
    --INSERT INTO premise(dID, crawlID, title) VALUES (discussion1Id, 'prem2', 'this is another premise') RETURNING pID INTO premise2Id;
    --INSERT INTO premise(dID, crawlID, title) VALUES (discussion2Id, 'prem3', 'premises are boring') RETURNING pID INTO premise3Id;
    --INSERT INTO premise(dID, crawlID, title) VALUES (discussion2Id, 'prem4', 'who cares about premises') RETURNING pID INTO premise4Id;
    --INSERT INTO premise(dID, crawlID, title) VALUES (discussion3Id, 'prem5', 'what the heck are premises') RETURNING pID INTO premise5Id;
    --INSERT INTO premise(dID, crawlID, title) VALUES (discussion3Id, 'prem6', 'a premise is something stange') RETURNING pID INTO premise6Id;
    --INSERT INTO premise(dID, crawlID, title) VALUES (discussion4Id, 'prem7', 'multiple arguments can share a premise') RETURNING pID INTO premise7Id;
    --INSERT INTO premise(dID, crawlID, title) VALUES (discussion4Id, 'prem8', 'arguments are also strange') RETURNING pID INTO premise8Id;
    --INSERT INTO premise(dID, crawlID, title) VALUES (discussion5Id, 'prem9', 'please forgive me german teacher') RETURNING pID INTO premise9Id;
    --INSERT INTO premise(dID, crawlID, title) VALUES (discussion5Id, 'prem10', 'I have no idea how arguments are structured') RETURNING pID INTO premise10Id;

    INSERT INTO argument(pID, crawlID, content, totalTokens, isPro) VALUES (premise1Id, 'crawlA1', 'arguments freedom fighter germany', 4, TRUE) RETURNING argID INTO argument1Id;
    INSERT INTO argument(pID, crawlID, content, totalTokens, isPro) VALUES (premise1Id, 'crawlA2', 'arguments fighter freedom germany', 4, FALSE) RETURNING argID INTO argument2Id;
    --INSERT INTO argument(pID, crawlID, content, totalTokens, isPro) VALUES (premise3Id, 'crawlA3', 'arguments are nothing but interesting', 30, TRUE) RETURNING argID INTO argument3Id;
    --INSERT INTO argument(pID, crawlID, content, totalTokens, isPro) VALUES (premise4Id, 'crawlA4', 'arguments are nothing but false', 40, FALSE) RETURNING argID INTO argument4Id;
    --INSERT INTO argument(pID, crawlID, content, totalTokens, isPro) VALUES (premise5Id, 'crawlA5', 'arguments are nothing but easy', 50, TRUE) RETURNING argID INTO argument5Id;
    --INSERT INTO argument(pID, crawlID, content, totalTokens, isPro) VALUES (premise6Id, 'crawlA6', 'arguments are nothing but hard', 60, FALSE) RETURNING argID INTO argument6Id;
    --INSERT INTO argument(pID, crawlID, content, totalTokens, isPro) VALUES (premise7Id, 'crawlA7', 'arguments are nothing but anything', 70, TRUE) RETURNING argID INTO argument7Id;
    --INSERT INTO argument(pID, crawlID, content, totalTokens, isPro) VALUES (premise8Id, 'crawlA8', 'arguments are nothing but difficult', 80, FALSE) RETURNING argID INTO argument8Id;
    --INSERT INTO argument(pID, crawlID, content, totalTokens, isPro) VALUES (premise9Id, 'crawlA9', 'arguments are nothing but blank', 90, TRUE) RETURNING argID INTO argument9Id;
    --INSERT INTO argument(pID, crawlID, content, totalTokens, isPro) VALUES (premise10Id, 'crawlA10', 'arguments are nothing but blabla', 100, FALSE) RETURNING argID INTO argument10Id;

    --INSERT INTO argument_index VALUES (helloTokenId, argument1Id, 0, 2, '{1, 2}');
    --INSERT INTO argument_index VALUES (helloTokenId, argument2Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO argument_index VALUES (helloTokenId, argument3Id, 0, 2, '{1, 4}');
    --INSERT INTO argument_index VALUES (helloTokenId, argument4Id, 0, 5, '{1, 4}');
    --INSERT INTO argument_index VALUES (helloTokenId, argument6Id, 0, 3, '{1, 2, 4}');
    --INSERT INTO argument_index VALUES (helloTokenId, argument7Id, 0, 2, '{1, 2}');
    --INSERT INTO argument_index VALUES (helloTokenId, argument10Id, 0, 3, '{0, 5, 6}');

    --INSERT INTO argument_index VALUES (thisTokenId, argument1Id, 0, 2, '{0, 2}');
    --INSERT INTO argument_index VALUES (thisTokenId, argument2Id, 0, 5, '{1, 2}');
    --INSERT INTO argument_index VALUES (thisTokenId, argument3Id, 0, 3, '{0, 2, 4}');
    --INSERT INTO argument_index VALUES (thisTokenId, argument4Id, 0, 2, '{1, 2}');
    --INSERT INTO argument_index VALUES (thisTokenId, argument5Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO argument_index VALUES (thisTokenId, argument6Id, 0, 2, '{1, 2}');
    --INSERT INTO argument_index VALUES (thisTokenId, argument7Id, 0, 5, '{1, 2}');
    --INSERT INTO argument_index VALUES (thisTokenId, argument8Id, 0, 3, '{1, 2, 7}');

    --INSERT INTO argument_index VALUES (isTokenId, argument1Id, 0, 2, '{1, 2}');
    --INSERT INTO argument_index VALUES (isTokenId, argument2Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO argument_index VALUES (isTokenId, argument3Id, 0, 2, '{0, 2}');
    --INSERT INTO argument_index VALUES (isTokenId, argument4Id, 0, 5, '{1, 2}');
    --INSERT INTO argument_index VALUES (isTokenId, argument5Id, 0, 3, '{0, 2, 4}');
    --INSERT INTO argument_index VALUES (isTokenId, argument6Id, 0, 2, '{1, 2}');
    --INSERT INTO argument_index VALUES (isTokenId, argument8Id, 0, 3, '{0, 3, 7}');
    --INSERT INTO argument_index VALUES (isTokenId, argument9Id, 0, 2, '{1, 2}');

    --INSERT INTO argument_index VALUES (aTokenId, argument1Id, 0, 5, '{1, 2}');
    --INSERT INTO argument_index VALUES (aTokenId, argument2Id, 0, 3, '{1, 2, 4}');
    --INSERT INTO argument_index VALUES (aTokenId, argument7Id, 0, 2, '{1, 2}');
    --INSERT INTO argument_index VALUES (aTokenId, argument8Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO argument_index VALUES (aTokenId, argument9Id, 0, 2, '{1, 2}');
    --INSERT INTO argument_index VALUES (aTokenId, argument10Id, 0, 5, '{1, 2}');
    --INSERT INTO argument_index VALUES (aTokenId, argument3Id, 0, 3, '{1, 2, 7}');

    --INSERT INTO argument_index VALUES (testTokenId, argument3Id, 0, 5, '{1, 2}');
    --INSERT INTO argument_index VALUES (testTokenId, argument4Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO argument_index VALUES (testTokenId, argument5Id, 0, 2, '{1, 2}');
    --INSERT INTO argument_index VALUES (testTokenId, argument6Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO argument_index VALUES (testTokenId, argument7Id, 0, 2, '{1, 4}');
    --INSERT INTO argument_index VALUES (testTokenId, argument8Id, 0, 5, '{1, 5}');
    --INSERT INTO argument_index VALUES (testTokenId, argument9Id, 0, 3, '{1, 2, 4}');

    --INSERT INTO argument_index VALUES (worldTokenId, argument3Id, 0, 5, '{1, 2}');
    --INSERT INTO argument_index VALUES (worldTokenId, argument4Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO argument_index VALUES (worldTokenId, argument5Id, 0, 2, '{1, 2}');
    --INSERT INTO argument_index VALUES (worldTokenId, argument6Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO argument_index VALUES (worldTokenId, argument7Id, 0, 2, '{1, 4}');
    --INSERT INTO argument_index VALUES (worldTokenId, argument8Id, 0, 5, '{1, 5}');
    --INSERT INTO argument_index VALUES (worldTokenId, argument9Id, 0, 3, '{1, 2, 4}');
    ----
    --INSERT INTO premise_index VALUES (helloTokenId, premise1Id, 0, 2, '{1, 2}');
    --INSERT INTO premise_index VALUES (helloTokenId, premise2Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO premise_index VALUES (helloTokenId, premise3Id, 0, 2, '{1, 4}');
    --INSERT INTO premise_index VALUES (helloTokenId, premise4Id, 0, 5, '{1, 4}');
    --INSERT INTO premise_index VALUES (helloTokenId, premise5Id, 0, 3, '{1, 2, 4}');
    --INSERT INTO premise_index VALUES (helloTokenId, premise6Id, 0, 2, '{1, 2}');
    --INSERT INTO premise_index VALUES (helloTokenId, premise7Id, 0, 3, '{0, 5, 6}');

    --INSERT INTO premise_index VALUES (thisTokenId, premise1Id, 0, 2, '{0, 2}');
    --INSERT INTO premise_index VALUES (thisTokenId, premise2Id, 0, 5, '{1, 2}');
    --INSERT INTO premise_index VALUES (thisTokenId, premise3Id, 0, 3, '{0, 2, 4}');
    --INSERT INTO premise_index VALUES (thisTokenId, premise4Id, 0, 2, '{1, 2}');
    --INSERT INTO premise_index VALUES (thisTokenId, premise5Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO premise_index VALUES (thisTokenId, premise7Id, 0, 2, '{1, 2}');
    --INSERT INTO premise_index VALUES (thisTokenId, premise8Id, 0, 5, '{1, 2}');
    --INSERT INTO premise_index VALUES (thisTokenId, premise9Id, 0, 3, '{1, 2, 7}');

    --INSERT INTO premise_index VALUES (isTokenId, premise1Id, 0, 2, '{1, 2}');
    --INSERT INTO premise_index VALUES (isTokenId, premise2Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO premise_index VALUES (isTokenId, premise3Id, 0, 2, '{0, 2}');
    --INSERT INTO premise_index VALUES (isTokenId, premise4Id, 0, 5, '{1, 2}');
    --INSERT INTO premise_index VALUES (isTokenId, premise5Id, 0, 3, '{0, 2, 4}');
    --INSERT INTO premise_index VALUES (isTokenId, premise7Id, 0, 2, '{1, 2}');
    --INSERT INTO premise_index VALUES (isTokenId, premise9Id, 0, 3, '{0, 3, 7}');
    --INSERT INTO premise_index VALUES (isTokenId, premise10Id, 0, 2, '{1, 2}');

    --INSERT INTO premise_index VALUES (aTokenId, premise1Id, 0, 5, '{1, 2}');
    --INSERT INTO premise_index VALUES (aTokenId, premise5Id, 0, 3, '{1, 2, 4}');
    --INSERT INTO premise_index VALUES (aTokenId, premise6Id, 0, 2, '{1, 2}');
    --INSERT INTO premise_index VALUES (aTokenId, premise7Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO premise_index VALUES (aTokenId, premise8Id, 0, 2, '{1, 2}');
    --INSERT INTO premise_index VALUES (aTokenId, premise9Id, 0, 5, '{1, 2}');
    --INSERT INTO premise_index VALUES (aTokenId, premise10Id, 0, 3, '{1, 2, 7}');

    --INSERT INTO premise_index VALUES (testTokenId, premise1Id, 0, 5, '{1, 2}');
    --INSERT INTO premise_index VALUES (testTokenId, premise3Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO premise_index VALUES (testTokenId, premise4Id, 0, 2, '{1, 2}');
    --INSERT INTO premise_index VALUES (testTokenId, premise5Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO premise_index VALUES (testTokenId, premise6Id, 0, 2, '{1, 4}');
    --INSERT INTO premise_index VALUES (testTokenId, premise7Id, 0, 5, '{1, 5}');
    --INSERT INTO premise_index VALUES (testTokenId, premise8Id, 0, 3, '{1, 2, 4}');

    --INSERT INTO premise_index VALUES (worldTokenId, premise1Id, 0, 5, '{1, 2}');
    --INSERT INTO premise_index VALUES (worldTokenId, premise3Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO premise_index VALUES (worldTokenId, premise4Id, 0, 2, '{1, 2}');
    --INSERT INTO premise_index VALUES (worldTokenId, premise5Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO premise_index VALUES (worldTokenId, premise6Id, 0, 2, '{1, 4}');
    --INSERT INTO premise_index VALUES (worldTokenId, premise7Id, 0, 5, '{1, 5}');
    --INSERT INTO premise_index VALUES (worldTokenId, premise8Id, 0, 3, '{1, 2, 4}');
    ---
    --INSERT INTO discussion_index VALUES (helloTokenId, discussion1Id, 0, 2, '{1, 2}');
    --INSERT INTO discussion_index VALUES (helloTokenId, discussion2Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO discussion_index VALUES (helloTokenId, discussion3Id, 0, 2, '{1, 4}');
    --INSERT INTO discussion_index VALUES (helloTokenId, discussion4Id, 0, 5, '{1, 4}');
    --INSERT INTO discussion_index VALUES (helloTokenId, discussion5Id, 0, 3, '{1, 2, 4}');

    --INSERT INTO discussion_index VALUES (thisTokenId, discussion1Id, 0, 2, '{0, 2}');
    --INSERT INTO discussion_index VALUES (thisTokenId, discussion2Id, 0, 5, '{1, 2}');
    --INSERT INTO discussion_index VALUES (thisTokenId, discussion4Id, 0, 3, '{0, 2, 4}');

    --INSERT INTO discussion_index VALUES (isTokenId, discussion1Id, 0, 3, '{0, 2, 4}');
    --INSERT INTO discussion_index VALUES (isTokenId, discussion2Id, 0, 2, '{1, 2}');
    --INSERT INTO discussion_index VALUES (isTokenId, discussion4Id, 0, 3, '{0, 3, 7}');
    --INSERT INTO discussion_index VALUES (isTokenId, discussion5Id, 0, 2, '{1, 2}');

    --INSERT INTO discussion_index VALUES (aTokenId, discussion1Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO discussion_index VALUES (aTokenId, discussion2Id, 0, 2, '{1, 2}');
    --INSERT INTO discussion_index VALUES (aTokenId, discussion3Id, 0, 5, '{1, 2}');
    --INSERT INTO discussion_index VALUES (aTokenId, discussion4Id, 0, 3, '{1, 2, 7}');

    --INSERT INTO discussion_index VALUES (testTokenId, discussion1Id, 0, 5, '{1, 2}');
    --INSERT INTO discussion_index VALUES (testTokenId, discussion2Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO discussion_index VALUES (testTokenId, discussion3Id, 0, 2, '{1, 2}');
    --INSERT INTO discussion_index VALUES (testTokenId, discussion4Id, 0, 5, '{1, 5}');
    --INSERT INTO discussion_index VALUES (testTokenId, discussion5Id, 0, 3, '{1, 2, 4}');

    --INSERT INTO discussion_index VALUES (worldTokenId, discussion1Id, 0, 5, '{1, 2}');
    --INSERT INTO discussion_index VALUES (worldTokenId, discussion2Id, 0, 3, '{0, 3, 5}');
    --INSERT INTO discussion_index VALUES (worldTokenId, discussion3Id, 0, 2, '{1, 2}');
    --INSERT INTO discussion_index VALUES (worldTokenId, discussion4Id, 0, 5, '{1, 5}');
    --INSERT INTO discussion_index VALUES (worldTokenId, discussion5Id, 0, 3, '{1, 2, 4}');
END $$;
