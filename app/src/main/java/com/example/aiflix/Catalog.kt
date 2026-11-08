package com.example.aiflix

import org.json.JSONArray

// بيانات فيلم واحد
data class MovieData(
    val titleAr: String,
    val titleEn: String,
    val isSeries: Boolean,
    val year: String,
    val rating: String,
    val genres: String,
    val cast: String,
    val synopsis: String,
    val poster: String,
    val backdrop: String,
    val imdbId: String,
    val streamUrl: String
)

// القائمة المختارة — نفس كتالوج موقع AIFLIX بالضبط
// روابط البث تستخدم سيرفر الموقع (SmashyStream) — نفس تجربة الويب
val CATALOG_JSON = """[
{"a":"أوبنهايمر","e":"Oppenheimer","t":"m","y":"2023","r":"8.9/10","g":"دراما، تاريخي، سيرة ذاتية","c":"Cillian Murphy، Emily Blunt","i":"tt15398776","p":"https://m.media-amazon.com/images/M/MV5BN2JkMDc5MGQtZjg3YS00NmFiLWIyZmQtZTJmNTM5MjVmYTQ4XkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"بين النجوم","e":"Interstellar","t":"m","y":"2014","r":"8.7/10","g":"خيال علمي، مغامرة، دراما","c":"Matthew McConaughey، Anne Hathaway","i":"tt0816692","p":"https://m.media-amazon.com/images/M/MV5BYzdjMDAxZGItMjI2My00ODA1LTlkNzItOWJjMDU5ZDJlYWY3XkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"استهلال (بداية)","e":"Inception","t":"m","y":"2010","r":"8.8/10","g":"أكشن، خيال علمي، إثارة","c":"Leonardo DiCaprio، Joseph Gordon-Levitt","i":"tt1375666","p":"https://m.media-amazon.com/images/M/MV5BMjAxMzY3NjcxNF5BMl5BanBnXkFtZTcwNTI5OTM0Mw@@._V1_FMjpg_UX600_.jpg"},
{"a":"فارس الظلام","e":"The Dark Knight","t":"m","y":"2008","r":"9.0/10","g":"أكشن، جريمة، دراما","c":"Christian Bale، Heath Ledger","i":"tt0468569","p":"https://m.media-amazon.com/images/M/MV5BMTMxNTMwODM0NF5BMl5BanBnXkFtZTcwODAyMTk2Mw@@._V1_FMjpg_UX600_.jpg"},
{"a":"نهوض فارس الظلام","e":"The Dark Knight Rises","t":"m","y":"2012","r":"8.4/10","g":"أكشن، دراما، إثارة","c":"Christian Bale، Tom Hardy","i":"tt1345836","p":"https://m.media-amazon.com/images/M/MV5BMTk4ODQzNDY3Ml5BMl5BanBnXkFtZTcwODA0NTM4Nw@@._V1_FMjpg_UX600_.jpg"},
{"a":"باتمان","e":"The Batman","t":"m","y":"2022","r":"7.8/10","g":"أكشن، جريمة، غموض","c":"Robert Pattinson، Zoë Kravitz","i":"tt1877830","p":"https://m.media-amazon.com/images/M/MV5BMmU5NGJlMzAtMGNmOC00YjJjLTgyMzUtNjAyYmE4Njg5YWMyXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"كثيب: الجزء الثاني","e":"Dune: Part Two","t":"m","y":"2024","r":"8.6/10","g":"أكشن، مغامرة، دراما، خيال علمي","c":"Timothée Chalamet، Zendaya","i":"tt15239678","p":"https://m.media-amazon.com/images/M/MV5BNTc0YmQxMjEtODI5MC00NjFiLTlkMWUtOGQ5NjFmYWUyZGJhXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"كثيب: الجزء الأول","e":"Dune: Part One","t":"m","y":"2021","r":"8.0/10","g":"أكشن، مغامرة، دراما، خيال علمي","c":"Timothée Chalamet، Rebecca Ferguson","i":"tt1160419","p":"https://m.media-amazon.com/images/M/MV5BNWIyNmU5MGYtZDZmNi00ZjAwLWJlYjgtZTc0ZGIxMDE4ZGYwXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"المصارع 2","e":"Gladiator II","t":"m","y":"2024","r":"6.8/10","g":"أكشن، مغامرة، دراما","c":"Paul Mescal، Denzel Washington","i":"tt9218128","p":"https://m.media-amazon.com/images/M/MV5BMWYzZTM5ZGQtY2U1My00NmM2LWFlMDEtMGNjYjdmOWM1MzA1XkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"المصارع","e":"Gladiator","t":"m","y":"2000","r":"8.5/10","g":"أكشن، مغامرة، دراما","c":"Russell Crowe، Joaquin Phoenix","i":"tt0172495","p":"https://m.media-amazon.com/images/M/MV5BYWQ4YmNjYjEtOWE1Zi00Y2U4LWI4NTAtMTU0MjkxNWQ1ZmJiXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"ديدبول وولفرين","e":"Deadpool & Wolverine","t":"m","y":"2024","r":"7.8/10","g":"أكشن، كوميديا، خيال علمي","c":"Ryan Reynolds، Hugh Jackman","i":"tt6263850","p":"https://m.media-amazon.com/images/M/MV5BZTk5ODY0MmQtMzA3Ni00NGY1LThiYzItZThiNjFiNDM4MTM3XkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"سبايدرمان: لا طريق للعودة","e":"Spider-Man: No Way Home","t":"m","y":"2021","r":"8.2/10","g":"أكشن، مغامرة، خيال علمي","c":"Tom Holland، Zendaya","i":"tt10872600","p":"https://m.media-amazon.com/images/M/MV5BMmFiZGZjMmEtMTA0Ni00MzA2LTljMTYtZGI2MGJmZWYzZTQ2XkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"سبايدرمان: عبر عالم العنكبوت","e":"Spider-Man: Across the Spider-Verse","t":"m","y":"2023","r":"8.6/10","g":"أنيميشن، أكشن، مغامرة","c":"Shameik Moore، Hailee Steinfeld","i":"tt9362722","p":"https://m.media-amazon.com/images/M/MV5BNThiZjA3MjItZGY5Ni00ZmJhLWEwN2EtOTBlYTA4Y2E0M2ZmXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"جون ويك: الفصل 4","e":"John Wick: Chapter 4","t":"m","y":"2023","r":"7.7/10","g":"أكشن، جريمة، إثارة","c":"Keanu Reeves، Laurence Fishburne","i":"tt10366206","p":"https://m.media-amazon.com/images/M/MV5BY2Q2ZmI5ZjUtNWVhMC00YzJkLTlmYjMtY2RmZDhkNzEzYjZhXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"جون ويك","e":"John Wick","t":"m","y":"2014","r":"7.4/10","g":"أكشن، جريمة، إثارة","c":"Keanu Reeves، Michael Nyqvist","i":"tt2911666","p":"https://m.media-amazon.com/images/M/MV5BMTU2NjA1ODgzMF5BMl5BanBnXkFtZTgwMTM2MTI4MjE@._V1_FMjpg_UX600_.jpg"},
{"a":"توب غان: مافريك","e":"Top Gun: Maverick","t":"m","y":"2022","r":"8.2/10","g":"أكشن، دراما","c":"Tom Cruise، Jennifer Connelly","i":"tt1745960","p":"https://m.media-amazon.com/images/M/MV5BMDBkZDNjMWEtOTdmMi00NmExLTg5MmMtNTFlYTJlNWY5YTdmXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"نادي القتال","e":"Fight Club","t":"m","y":"1999","r":"8.8/10","g":"دراما","c":"Brad Pitt، Edward Norton","i":"tt0137523","p":"https://m.media-amazon.com/images/M/MV5BOTgyOGQ1NDItNGU3Ny00MjU3LTg2YWEtNmEyYjBiMjI1Y2M5XkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"خيال رخيص","e":"Pulp Fiction","t":"m","y":"1994","r":"8.9/10","g":"جريمة، دراما","c":"John Travolta، Uma Thurman","i":"tt0110912","p":"https://m.media-amazon.com/images/M/MV5BYTViYTE3ZGQtNDBlMC00ZTAyLTkyODMtZGRiZDg0MjA2YThkXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"الخلاص من شاوشانك","e":"The Shawshank Redemption","t":"m","y":"1994","r":"9.3/10","g":"دراما","c":"Tim Robbins، Morgan Freeman","i":"tt0111161","p":"https://m.media-amazon.com/images/M/MV5BMDAyY2FhYjctNDc5OS00MDNlLThiMGUtY2UxYWVkNGY2ZjljXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"الماتريكس","e":"The Matrix","t":"m","y":"1999","r":"8.7/10","g":"أكشن، خيال علمي","c":"Keanu Reeves، Laurence Fishburne","i":"tt0133093","p":"https://m.media-amazon.com/images/M/MV5BN2NmN2VhMTQtMDNiOS00NDlhLTliMjgtODE2ZTY0ODQyNDRhXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"العراب","e":"The Godfather","t":"m","y":"1972","r":"9.2/10","g":"جريمة، دراما","c":"Marlon Brando، Al Pacino","i":"tt0068646","p":"https://m.media-amazon.com/images/M/MV5BNGEwYjgwOGQtYjg5ZS00Njc1LTk2ZGEtM2QwZWQ2NjdhZTE5XkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"المنتقمون: نهاية اللعبة","e":"Avengers: Endgame","t":"m","y":"2019","r":"8.4/10","g":"أكشن، مغامرة، خيال علمي","c":"Robert Downey Jr.، Chris Evans","i":"tt4154796","p":"https://m.media-amazon.com/images/M/MV5BMWEyNDM2ZmQtMmFkNi00MTQ1LTk1MjItMzdlZGJlYmIyYzZlXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"المنتقمون: الحرب اللانهائية","e":"Avengers: Infinity War","t":"m","y":"2018","r":"8.4/10","g":"أكشن، مغامرة، خيال علمي","c":"Robert Downey Jr.، Chris Hemsworth","i":"tt4154756","p":"https://m.media-amazon.com/images/M/MV5BMjMxNjY2MDU1OV5BMl5BanBnXkFtZTgwNzY1MTUwNTM@._V1_FMjpg_UX600_.jpg"},
{"a":"أفاتار: طريق الماء","e":"Avatar: The Way of Water","t":"m","y":"2022","r":"7.6/10","g":"أكشن، مغامرة، خيال علمي","c":"Sam Worthington، Zoe Saldaña","i":"tt1630029","p":"https://m.media-amazon.com/images/M/MV5BNWI0Y2NkOWEtMmM2OC00MjQ3LWI1YzItZGQxYzQ3NzI4NWZmXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"جوكر","e":"Joker","t":"m","y":"2019","r":"8.4/10","g":"جريمة، دراما، إثارة","c":"Joaquin Phoenix، Robert De Niro","i":"tt7286456","p":"https://m.media-amazon.com/images/M/MV5BNzY3OWQ5NDktNWQ2OC00ZjdlLThkMmItMDhhNDk3NTFiZGU4XkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"سبعة","e":"Seven","t":"m","y":"1995","r":"8.6/10","g":"جريمة، دراما، غموض","c":"Morgan Freeman، Brad Pitt","i":"tt0114369","p":"https://m.media-amazon.com/images/M/MV5BY2IzNzMxZjctZjUxZi00YzAxLTk3ZjMtODFjODdhMDU5NDM1XkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"فورست غامب","e":"Forrest Gump","t":"m","y":"1994","r":"8.8/10","g":"دراما، رومانسية","c":"Tom Hanks، Robin Wright","i":"tt0109830","p":"https://m.media-amazon.com/images/M/MV5BNDYwNzVjMTItZmU5YS00YjQ5LTljYjgtMjY2NDVmYWMyNWFmXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"فاست إكس","e":"Fast X","t":"m","y":"2023","r":"5.8/10","g":"أكشن، مغامرة، جريمة","c":"Vin Diesel، Michelle Rodriguez","i":"tt5433140","p":"https://m.media-amazon.com/images/M/MV5BYzEwZjczOTktYzU1OS00YjJlLTgyY2UtNWEzODBlN2RjZDEwXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"السرعة والغضب 7","e":"Furious 7","t":"m","y":"2015","r":"7.1/10","g":"أكشن، جريمة، إثارة","c":"Vin Diesel، Paul Walker","i":"tt2820852","p":"https://m.media-amazon.com/images/M/MV5BMTQxOTA2NDUzOV5BMl5BanBnXkFtZTgwNzY2MTMxMzE@._V1_FMjpg_UX600_.jpg"},
{"a":"بريكنغ باد","e":"Breaking Bad","t":"s","y":"2008","r":"9.5/10","g":"جريمة، دراما، إثارة","c":"Bryan Cranston، Aaron Paul","i":"tt0903747","p":"https://m.media-amazon.com/images/M/MV5BOWE4NTc3YmYtNmU2Mi00ZjhkLWE1MTItZmM1M2U1ODU3YjFlXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"من الأفضل الاتصال بسول","e":"Better Call Saul","t":"s","y":"2015","r":"9.0/10","g":"جريمة، دراما","c":"Bob Odenkirk، Rhea Seehorn","i":"tt3032476","p":"https://m.media-amazon.com/images/M/MV5BMTAxOTQ0MjUzMzJeQTJeQWpwZ15BbWU4MDY0NTAxNzMx._V1_FMjpg_UX600_.jpg"},
{"a":"ذا بويز (الرفاق)","e":"The Boys","t":"s","y":"2019","r":"8.7/10","g":"أكشن، كوميديا، دراما","c":"Karl Urban، Jack Quaid","i":"tt1190634","p":"https://m.media-amazon.com/images/M/MV5BZjU4OWNiYzQtMzc1NS00NjZlLTgyYTctZWY4ZmEzMTkxYjA4XkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"الجيل الخامس","e":"Gen V","t":"s","y":"2023","r":"7.7/10","g":"أكشن، كوميديا، دراما","c":"Jaz Sinclair، Lizze Broadway","i":"tt13159924","p":"https://m.media-amazon.com/images/M/MV5BYWU4YTljYmItYjczNS00MWY0LWI3NjctZjJhYjU3NWNmNWZhXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"صراع العروش","e":"Game of Thrones","t":"s","y":"2011","r":"9.2/10","g":"أكشن، مغامرة، دراما، فانتازيا","c":"Emilia Clarke، Peter Dinklage","i":"tt0944947","p":"https://m.media-amazon.com/images/M/MV5BNGYxOGJkMjItZjVkZC00OGEzLWExNjktOTZmNGZhZmRlMTk2XkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"آل التنين","e":"House of the Dragon","t":"s","y":"2022","r":"8.4/10","g":"أكشن، مغامرة، دراما، فانتازيا","c":"Matt Smith، Emma D'Arcy","i":"tt11198330","p":"https://m.media-amazon.com/images/M/MV5BOWU2ZDA0M2EtNWEyYy00MWUwLWI4NjAtYzkxMDFjYzZkNGFiXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"أشياء غريبة","e":"Stranger Things","t":"s","y":"2016","r":"8.7/10","g":"دراما، فانتازيا، رعب، خيال علمي","c":"Millie Bobby Brown، Finn Wolfhard","i":"tt4574334","p":"https://m.media-amazon.com/images/M/MV5BMjEzMDAxOTUyMV5BMl5BanBnXkFtZTgwNzAxMzYzOTE@._V1_FMjpg_UX600_.jpg"},
{"a":"تشيرنوبيل","e":"Chernobyl","t":"s","y":"2019","r":"9.3/10","g":"دراما، تاريخي، إثارة","c":"Jared Harris، Jessie Buckley","i":"tt7366338","p":"https://m.media-amazon.com/images/M/MV5BNzU0OTI4YTQtNGQ1ZS00ZjA4LTg3MTMtZjkyZWNjN2RiZDJmXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"بيكي بلايندرز","e":"Peaky Blinders","t":"s","y":"2013","r":"8.8/10","g":"جريمة، دراما","c":"Cillian Murphy، Paul Anderson","i":"tt2442560","p":"https://m.media-amazon.com/images/M/MV5BOGM0NGY3ZmItOGE2ZC00OWIxLTk0N2EtZWY4Yzg3ZDlhNGI3XkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"ذا لاست أوف أس","e":"The Last of Us","t":"s","y":"2023","r":"8.7/10","g":"أكشن، مغامرة، دراما، رعب","c":"Bella Ramsey، Pedro Pascal","i":"tt3581920","p":"https://m.media-amazon.com/images/M/MV5BYWI3ODJlMzktY2U5NC00ZjdlLWE1MGItNWQxZDk3NWNjN2RhXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"شوجون","e":"Shōgun","t":"s","y":"2024","r":"8.7/10","g":"مغامرة، دراما، تاريخي","c":"Cosmo Jarvis، Hiroyuki Sanada","i":"tt2788316","p":"https://m.media-amazon.com/images/M/MV5BZmJkMDRjYzEtMWI3Ny00OWE3LWJlNTItMGQ1MTQzMzc3NDY5XkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"فول أوت","e":"Fallout","t":"s","y":"2024","r":"8.4/10","g":"أكشن، مغادمة، دراما، خيال علمي","c":"Ella Purnell، Aaron Moten","i":"tt12637874","p":"https://m.media-amazon.com/images/M/MV5BYzE1MTZmY2ItMTgxNi00OGU5LTk4NTItMmY2ODFhYzhjZjRkXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"انفصال (سيفرانس)","e":"Severance","t":"s","y":"2022","r":"8.7/10","g":"دراما، غموض، خيال علمي، إثارة","c":"Adam Scott، Britt Lower","i":"tt11280740","p":"https://m.media-amazon.com/images/M/MV5BZDI5YzJhODQtMzQyNy00YWNmLWIxMjUtNDBjNjA5YWRjMzExXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"لعبة الحبار","e":"Squid Game","t":"s","y":"2021","r":"8.0/10","g":"أكشن، دراما، غموض، إثارة","c":"Lee Jung-jae، Wi Ha-joon","i":"tt10919420","p":"https://m.media-amazon.com/images/M/MV5BMDcxYjg1OTctYmUxZi00ZjhjLWIwNTMtNmI2M2Y4OTU1YWU2XkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"البطريق","e":"The Penguin","t":"s","y":"2024","r":"8.8/10","g":"جريمة، دراما","c":"Colin Farrell، Cristin Milioti","i":"tt15435876","p":"https://m.media-amazon.com/images/M/MV5BYmU3MzYzOWEtOTdkZi00YzA1LTliMzQtNDdiZDBjY2FiNmFlXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"الخلاصة (سكسيشن)","e":"Succession","t":"s","y":"2018","r":"8.9/10","g":"دراما","c":"Nicholas Braun، Brian Cox","i":"tt7660850","p":"https://m.media-amazon.com/images/M/MV5BYTY4YTVkY2QtMjRmOS00YzliLWIxOWQtMTdkOTVkN2UzODNmXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"آل سوبرانو","e":"The Sopranos","t":"s","y":"1999","r":"9.2/10","g":"جريمة، دراما","c":"James Gandolfini، Lorraine Bracco","i":"tt0141842","p":"https://m.media-amazon.com/images/M/MV5BZjYwNWQwOTMtMzYwOS00NTcxLWFhNTYtNGQ4YjdmYTcyNDFkXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"ديكستر","e":"Dexter","t":"s","y":"2006","r":"8.7/10","g":"جريمة، دراما، غموض","c":"Michael C. Hall، Jennifer Carpenter","i":"tt0773262","p":"https://m.media-amazon.com/images/M/MV5BNTE5ZGI2N2UtYmFiMi00ZGIxLWI1ZTMtYWJkZDYxNDZiOTQwXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"شارلوك","e":"Sherlock","t":"s","y":"2010","r":"9.1/10","g":"جريمة، دراما، غموض","c":"Benedict Cumberbatch، Martin Freeman","i":"tt1475582","p":"https://m.media-amazon.com/images/M/MV5BMjRhZDdjMDYtMTg5Yy00NGI4LWI3ZDgtYjNkNDc4MzJkNDRjXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"ريك ومورتي","e":"Rick and Morty","t":"s","y":"2013","r":"9.1/10","g":"أنيميشن، مغامرة، كوميديا، خيال علمي","c":"Chris Parnell، Spencer Gramser","i":"tt2861424","p":"https://m.media-amazon.com/images/M/MV5BZGQyZjk2MzMtMTcyNC00NGU3LTlmNjItNDExMWM4ZDFhYmQ2XkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"},
{"a":"أركين","e":"Arcane","t":"s","y":"2021","r":"9.0/10","g":"أنيميشن، أكشن، مغامرة، فانتازيا","c":"Kevin Alejandro، Hailee Steinfeld","i":"tt11126994","p":"https://m.media-amazon.com/images/M/MV5BYjA2NzhlMDItNWRmZC00MzRjLWE3ZjAtZjBlZDAwOWY2ODdjXkEyXkFqcGc@._V1_FMjpg_UX600_.jpg"}
]"""

fun catalogMovies(): List<MovieData> {
    val list = ArrayList<MovieData>()
    val arr = JSONArray(CATALOG_JSON)
    val defaultSynopsis = "عمل رائع متاح الآن للبث المباشر الفوري بجودة فائقة وبدون إعلانات."
    for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        val imdb = o.getString("i")
        val isSeries = o.getString("t") == "s"
        val poster = o.getString("p")
        val backdrop = poster.replace("_FMjpg_UX600_", "")
        val stream = if (isSeries) {
            "https://embed.smashystream.com/playere.php?imdb=" + imdb + "&season=1&episode=1"
        } else {
            "https://embed.smashystream.com/playere.php?imdb=" + imdb
        }
        list.add(
            MovieData(
                titleAr = o.getString("a"),
                titleEn = o.getString("e"),
                isSeries = isSeries,
                year = o.getString("y"),
                rating = o.getString("r"),
                genres = o.getString("g"),
                cast = o.getString("c"),
                synopsis = defaultSynopsis,
                poster = poster,
                backdrop = backdrop,
                imdbId = imdb,
                streamUrl = stream
            )
        )
    }
    return list
}
