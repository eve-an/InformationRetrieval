drop type if exists documenttype;
create type documenttype as enum ('argument', 'premise', 'discussion');
