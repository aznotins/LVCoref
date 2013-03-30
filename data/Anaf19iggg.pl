%-------------------- GOLD korpuss  --------------------------------
go(C) :- 
asserta(corp(-9,x)),
take(C),
flag(nunu,Y,Y),Z is Y-1,writeln([Z,'last training sentence']).

gof :- 
asserta(corp(-9,x)),
take('SofijasPasaule1996_11-28-dep-unlabeled.conll'),
/* take('AndraNeiburga-StumStum2004_0001-0430-dep-unlabeled.conll'),
take('AndraNeiburga-StumStum2004_2460-2724-dep-unlabeled.conll'),
take('c60-dep-unlabeled.conll'),
take('d62-JaanisEinfelds-Melis-dep-unlabeled.conll'),
take('Leta_paraugs1-dep-unlabeled.conll'),
take('SaliktsTeikums_121-dep-unlabeled.conll'),
take('Teikumi_001-297-dep-unlabeled.conll'),
take('z46-dep-unlabeled.conll'),
take('zeens-dep-unlabeled.conll'), */
flag(nunu,Y,Y),Z is Y-1,writeln([Z,'last training sentence']).


take(NAME) :- see(NAME),not(read_text(A)),corp(X,Y)/*,writeln([X,Y])*/,seen,!.

proc([[ID,FORM,LEMMA,CPOSTAG,POSTAG,FEATS,HEAD,DEPREL,PHEAD,PDEPREL]|AA],
     [[FORM|B1],[[HEAD2,ID2]|B2],[POSTAG|B3],[CPOSTAG|B4],[LEMMA|B5]]) :- 
           !,proc(AA,[B1,B2,B3,B4,B5]),
           atom_number(HEAD,HEAD1),atom_number(ID,ID1),
           HEAD2 is HEAD1+2,ID2 is ID1+2.

proc(_,[[],[],[],[],[]]).

read_text([B|A]) :- read_sent(B),
proc([['-1','*','*',start,start,start,'-2',a,a,a],['0','**','**',stop,stop,stop,'-1',a,a,a]|B],R),
not(R=[[_,_],[_,_],[_,_],[_,_],[_,_]]),  R=[R1,[_|R2],R3,R4,R5], RR= [R1,R2,R3,R4,R5], (not(corp(_,RR)),!,flag(nunu,N,N+1),assertz(corp(N,RR));R=R),
(B=[_],!,A=[];read_text(A)).
read_sent([B|A]) :- read_line(B),(B=[_],!,A=[];read_sent(A)).
read_line([B|A]) :- token(B,X),(X=10,!,A=[];X=(-1),!,A=[];read_line(A)).


token(A,X) :- geti(R),append(RR,[X],R),atom_codes(A,RR).
geti([X|A]) :- get_code(X),(X=9,!,A=[];X=10,!,A=[];X=(-1),!,A=[];geti(A)).

%------------------------------- KAMOLINA -----------------------------------------------------------------
% Tagad jasabuve VISU teikumu koks, kura tad apstaigasu un atzimesu visu ko.
% Visiem Mentions iedod numurus (verbiem etc - nee), un tad virsotnes liek aizliegto numurus // atlauto numurus 
% (vairak ka viens caur tranzsitivitati)
% -- sie nedrikst parklaties!
% Beigas liek NULL vai atrod koka tuvako atlauto
% Tikai pasas beigas taisa transitive closure.

% vajadzes rules, kas so koku aizpilda


tree :- assert(filt(x,x,x)),corp(J,[PLAIN,GOLD,FPOS,SPOS,LEMMA]), member([H,C],GOLD),(H=1,JH is (J-1)*1000+2;H>1,JH is J*1000+H), JC is J*1000+C, 
assertz(arc(JH,JC)),nth1(C,PLAIN,PL),nth1(C,FPOS,FP),nth1(C,SPOS,SP), nth1(C,LEMMA,L),
mention([PL,FP,SP,L],MM),assertz(node(JC,MM,[PL,FP,SP,L])),assertz(filt(JC,[],[])),fail.

printTree :- node(JC,MM,[PL,FP,SP,L]),filtR(JC,X,Y),
(PL='**',nl; not(PL='**'),write(PL), (X=[XX],(not(XX=xxx),write([XX]);XX=xxx),write(' ');not(X=[XX]),write(' '))),fail.

prf(A) :- tell(A),current_output(S),set_stream(S,encoding(utf8)), not(printTree), told.

clo :- filt(N,[],Y),not(node(N,none,_)),!,flag(kriamo,NN,NN), flag(amokri,KRI,KRI),
    not(((filtW(N,[NN],Y),member(YY,Y),closure(NN,YY),fail; toto(NN,N),fail))),flag(amokri,AMO,AMO),(KRI<AMO,flag(kriamo,NN,NN+1);KRI=AMO,filtW(N,[xxx],Y)),clo.


toto(NN,N) :- filt(N2,[],Y2),member(N,Y2),!,
    not((filtW(N2,[NN],Y2), node(N2,_,[W|_]),flag(amokri,KRI,KRI+1),writeln(['***toto****',W,N2,NN]),  (member(YY,Y2),closure(NN,YY),fail; toto(NN,N2),fail))),toto(NN,N).

closure(NN,N) :- filt(N,[],Y),!,
    not((filtW(N,[NN],Y), node(N,_,[W|_]),flag(amokri,KRI,KRI+1),writeln(['***clos****',W,N,NN]),(member(YY,Y),closure(NN,YY),fail; toto(NN,N),fail))),closure(NN,N).



filtR(N,X,Y) :- filt(N,X,Y).

filtW(N,X,Y) :- filt(N,XX,YY),retract(filt(N,XX,YY)),assertz(filt(N,X,Y)).



mention([PL,FP,SP,L],proper) :- atom_chars(FP,[n,p|_]),!.
mention([PL,FP,SP,L],common) :- atom_chars(FP,[n,c|_]),not(exclude(L)),!.
mention([PL,FP,SP,L],pronoun) :- atom_chars(FP,[p|_]),ner(pronoun,_,LL),member(L,LL),!.
mention(_,none).

exclude('skaits').
exclude('vārds').
exclude('gals').
exclude('laiks').
exclude('interese').
exclude('gadījums').
exclude('reize').
exclude('sākums').
exclude('priekša').
exclude('vieta').

ka :- gof,tree.
ka :- rule2. % sai jabut pirmajai rulei atrdarbibas del
ka :- rule1.
ka :- rule3.
ka :- clo.
ka :- printTree.

% Neskaidrais - ka kodet to, ka vardam jau atrasts antecedents, jeb palicis NULL ???
% Varbut tas nav butiski - pietiek, ka visiem pronauniem ir antecedenti.

%rule4 - filtresana kas nevar but antecedents, ieskaitot virzienus.

%rule3 - pronauni un NER kategorijas.

enum(N,K,Z) :- NN is K-N,length(L,NN),append(A,_,L),length(A,ZZ), Z is ZZ+N.
order7(H,H2,M,LIM) :- enum(1,LIM,N),length(L,N),order(H,H2,M,L).

order(H,H,LIM,LIM).
order(H,H2,M,LIM) :- length(M,MN),length(LIM,LN),MN<LN,arc(H3,H),not(member([H3,H],M)),order(H3,H2,[[H3,H]|M],LIM).
order(H,H2,M,LIM) :- length(M,MN),length(LIM,LN),MN<LN,arc(H,H3),not(member([H,H3],M)),order(H3,H2,[[H,H3]|M],LIM).


% Subject un object nevar co-refret!!!
rule3 :- node(H,pronoun,[PL,FP,SP,L]),filtR(H,_,[]),atom_chars(FP,[p,_,_,M,P|_]),order7(H,H2,[],20),H2<H,(H-H2)<5500,
node(H2,NN,[PL2,FP2,SP2,L2]), (NN=common;NN=proper), atom_chars(FP2,[n,_,M,P|_]),
ner(pronoun,T,TT),member(L,TT),ner(NN,T,TTT),member(L2,TTT),
!, filtR(H,X,Y),filtW(H,X,[H2|Y]),writeln([PL,PL2]),rule3.


% noverst subject-object anaforu
% pielikt ruli X ir Y - tētis ir kapteinis
rule2 :- node(H,NN,[PL,FP,SP,L]),appositive(NN,_),filtR(H,_,ZZZ),node(H2,NN2,[PL2,FP2,SP2,L2]),
(H2>H,(H2-H)<29500 ,NN2=common;NN2=proper),not(member(H2,ZZZ)),
(sino(LLL),subset([L,L2],LLL);L=L2),
atom_chars(FP,[n,_,M,P,G|_]),atom_chars(FP2,[n,_,M,P,G2|_]),not(genitiveBad(H)),not(genitiveBad(H2)),
filtR(H2,X,Y),filtW(H2,X,[H|Y]),writeln([PL,PL2]),fail.

genitiveBad(H) :- node(H,common,[PL,FP,SP,L]),atom_chars(FP,[n,_,M,P,g|_]),arc(H2,H),
                 node(H2,N2,[PL2,FP2,SP2,L2]),atom_chars(FP2,[n|_]).
% genitivs papildinatajs parsvara ir "viens-vārds" jeb "nosaukums" un tāpēc neveido anaforu; pilns FE vai caur prievardu ir OK


% te butu japieliek genetivenu un ipasibu salidzinasanu - koka masina un dzelzs masina
% neiet talak par ieprieksejo teikumu common-noun-iem, bet papildinat ar sinonimiem kategoriju ietvaros - YYYYYYYYYYY-tik ja viens proper.
% nemt vera latviesu artikulus "kadu dienu" un "taja dienā"

rule1 :-arc(H,C),node(H,NN,[PL,FP,SP,L]),node(C,NNN,[PL2,FP2,SP2,L2]),appositive(NN,NNN),
atom_chars(FP,[n,_,M,P,N|_]),atom_chars(FP2,[n,_,M,P,N|_]),genitiveTest(N,L,L2,NN,NNN),filtR(H,X,Y),filtW(H,X,[C|Y]),writeln([PL,PL2]),fail.

appositive(proper,proper) :- !.
appositive(proper,common) :- !.
appositive(common,proper) :- !.

% sos no Wikipedia testiem caur "but" macas -- un tos velk kopa YYYYYYYYYYYY-tik ja viens proper.
genitiveTest(g,A,B,NN,NNN) :- subset([A,B],['Jelgava',pilsēta]), !.
genitiveTest(g,A,B,NN,NNN) :- subset([A,B],['Latvija',republika]), !.
% genitiveTest(g,A,B,NN,NNN) :- subset([A,B],['Latvija',universitāte]), !.
genitiveTest(g,A,B,proper,proper) :- !.
genitiveTest(g,A,B,NN,NNN) :- !,writeln(['---fail---',A,B]),fail.
genitiveTest(_,_,_,NN,NNN).

anaf(A,B) :- asserta(pron(x)),enum(A,B,J),corp(J,[PLAIN,GOLD,POS,SPOS,LEMMA]), nth1(N,SPOS,p), nth1(N,PLAIN,P),nth1(N,LEMMA,L),nth1(N,POS,S),
             writeln([P,L,S]),assertz(pron([P,L,S])),fail.

prons :- asserta(pron(x)),corp(J,[PLAIN,GOLD,POS,SPOS,LEMMA]), nth1(N,SPOS,p), nth1(N,PLAIN,P),nth1(N,LEMMA,L),nth1(N,POS,S),
             writeln([P,L,S]),assertz(pron([P,L,S])),fail.

nps :- asserta(pron(x)),corp(J,[PLAIN,GOLD,POS,SPOS,LEMMA]), nth1(N,POS,PP),atom_chars(PP,[n,p|_]), nth1(N,PLAIN,P),nth1(N,LEMMA,L),nth1(N,POS,S),
             writeln([P,L,S]),assertz(pron([P,L,S])),fail.

group :- findall(A,(pron([P,A,S])),R),list_to_set(R,RR),writeln(RR).

gr(Z) :- findall(A,(pron([P,Z,S]),A=[P,S]),R),list_to_set(R,RR),writeln(RR).

ner(proper,person,['Andra','Neiburga','Anrī','Kartjē','Bresons','Dievs','Opis','Dieviņš','Teodors','Raimonds','Pauls',
'Gulbīši','Robis','Kārta','Opītis','Sanī','Čingishans','Solvita','Mīlnieki','Mišels','Amoss','Ozs','Agnese',
'Aīda','Miro','Zemesmāte','Didzis','Valdis',
'Sprūdžs','Rāviņš','Edmunds','Andris','Irēna','Škutāne','Egita','Diure','Sofija','Amundsena','Jūruna',
'Āboliņš','Amudsena','Šerekāns','Zeltīte','Sarkangalvīte','Pēteris','Smits','Smulē','Govinds','Anna',
'Knutsena','Sinēve','Klods','Debisī','Marija','Hilda','Mellere','Knāga','Mellers','Knāgs','Andersens',
'Nīlsens','Jepsens','Tomass','Balvis','Emīls','Lepters','Armands','Viktors',
'Sezārija','Evora','Nora','Džonsa','Tēsejs','Ariadne','Oto','Čingizhans',
'Viņķeļi','Francis','Ella','Aleksandra','Tristāns','Izolde','Andžs','Tīna','Zumpji','Dārta',
'Augustīns','Terēze','Mia','Kopeloviča','Žukovs','Oļehnoviča','Kravale-Pauliņa','Praulīte',
'Gedrovics','Rauckiene','Samuseviča','Sīle','Opincāns','Juta','Tiešis','Šaltenis',
'Dzemida','Maija','Kārlis','Kabacis','Jānis','Čārlzs','Juris','Emīlija']).
ner(common,person,['bērns','pētnieks','zinātnieks','deputāts','meitene','zēns',tētis, tēvs,mamma, māte]).
ner(pronoun,person,['kurš','kura','es','tu','viņš','viņa']).


ner(proper,location,['Visums','Zeme','Mēness','Latvija','Palestīna','Izraēla','Austrālija','Rīga','Vācija','Sibīrija',
'Maskava','Holande','Krievija','Šmerlis','Holivuda','Rozes','Mežotne','Jelgava','Zemgale','Jēkbapils','Jēkabpils',
'Ēdene','Slēpnis','Londona','Jūrmala','Eiropa','Lietuva','Meksika','Francija','Ungārija','Čehija','Florida','Somija']).
ner(common,location,[iela,upe,jūra,ceļš,skola]).
ner(pronoun,location,['tas','tā','kas']). % "tajā" no šiem izlokas?

ner(proper,org,['Dunhill','Rimi','Adidas','Pilsētsaimniecība','Baltkonsults']).
ner(common,org,['kompānija','uzņēmums']).
ner(pronoun,org,['tas','tā','kas','kurš','kura']).

ner(proper,time,['Ziemsvētki','Meteņi','Lieldienas','Pēteri','Jāņi','Kumēdiņi']).
ner(common,time,['tagad','šodien']).
ner(pronoun,time,['tad','toreiz']).

%šiem ir tilpums, citi vietniekvārdi. Ja būtu korpuss, šos varētu ar learning mācīties
ner(proper,thing,['Bībele']).
ner(common,thing,[pagrieziens,aploksne,kastīte,zīmīte,māja,vieta,vēstule,vēstulīte]).
ner(pronoun,thing,[kurš,kura,tas,tā]). % "tur" ir apstāklis, nevis vietniekvārds??

% sie loti konkreti sinonimi. Plasakas anaforas pec NER tipa Person,...
sino([prezidents,'Obama']).
sino([dziedātājs,'Reiniks']).
sino([tēvs,tētis]).
sino([vēstule,vēstulīte,aploksne]).
sino([vēstuļkaste,vēstuļkastīte]).
sino([runcis,mincis]).
sino([māte,māmiņa]).

%Papildināt ar sinonīmu virknītēm - cekot varēs ar subset funkciju easy
% sie japapildina ar hiponimiem: Florida, štats (ne katrs štats ir florida, lai gan vienā rakstā visdrīzāk ir

/*
1) Papildinātāji nevar koreferencēties: pulstenis no tā, throught of his
2) Artikuli kāds, tas - var pārgriezt koreferencu kediti. Ari papildinataji var pargriezt: Iranian officials/Korean officials
3) Jānis un Pēteris, viņa padomnieks - i-within-i vina-->Janis (third-child NP == non-anaphora)
4) Unknowm NER tipi pielikt ka pedejo glabinu
5) Lietot Frame FE semType: viņa tam (ceļam) piekrīt - nesakrīt FE tips.
6) Pilnas un pamazinamas formas naforejas automatiski
7) Macities sinonimus/hiponimus - gan no tekosa teksta pirms anaforu risinasanas, gan no citeim tekstiem nemarketiem(x ir Y; x medz saukt Y)
8) Nelaut prieksmetam un objektam anaforeties; ari citiem verba tiesiem locekliem (FR). Iznemums: kurmis apraka sevi.
9) "maza zīmīte, ne lielāka par aploksni, kurā tā atradās" -- kā "tā"-->zīmīte nevis aploksne?
10) ideāli NER/TYPE likt visai anaforu keditei dinamiski - lai jaunos coreferentus pielauj tikai ar kedites tipu. Ta var ari pronouns uz pronounu atsaukties - ja tas ko palidz. SO TIPU ARI DRUKAT PIE KOREF INDEKSA!
11) Globālo NER indeksu ieviest kā Prolog predikātu - ar fiksētu tipu un references numuru. Vai to realizet pie sinonimiem vai pie NER tipiem ipasiem (apakstips NER?)
12) pielikt masinmacisanos beigas, lai sos leksiskos sarakstus samacas optimali no korpusa.
14) caur Apopositiviem iegutas sakaribas likt DB (sinonimi,hiponimi) - tas palidzes nakamajos tekstos: Bethovens, Ludvigs, komponists
15) Ja anaforu datu bazi menedze pilnigi automatiski starp tekstiem, tad jau var vairs to nedalit pa rakstiem - lai ir viena liela nafaroru datubaze. Tad arī Dāvja intefeiss mainās.
16) iPhone izklausīt
17) savilk visus leksiskos input-data vienuviet
18) Vienīgais , ko viņa[166] tajā[8] atrada , bija maza zīmīte[11] , ne lielāka par aploksni[8] , kurā[8] tā[8] atradās . 
19)  ne sveiciena[12] 
20) Jā gan — vēstule[7] bija adresēta viņai . -- radius 20 increase
21) Zvērnīca[14] ir vieta , kurā[3] savākti dažādi dzīvnieki[15] , un patiešām — Sofija[140] bija gluži mierā ar savu dzīvnieku kolekciju . 
22) Kā būtu , ja viņu[140] sauktu kādā citā vārdā ? 
    Piemēram , par Annu[144] Knutsenu[144] . 
    Vai viņa[144] būtu tādā gadījumā cita ? 
23) Viņa[140] nostājās spoguļa priekšā un skatījās sev acīs[17] . 
*/

