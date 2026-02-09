SELECT (date2::date - date1::date) as datediff from (SELECT date_status date1,id_probleme from probleme_status ps
join status s on s.id_status=ps.id_status
where id_probleme=121 and s.nom='nouveau') as stat_nouveau 
join 
(SELECT date_status date2,id_probleme from probleme_status ps
join status s on s.id_status=ps.id_status
where id_probleme=121 and s.nom='termine') as stat_termine
on stat_nouveau.id_probleme=stat_termine.id_probleme

select 

DATEDIFF(Day,date1,date2) 