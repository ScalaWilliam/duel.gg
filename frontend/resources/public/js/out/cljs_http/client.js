// Compiled by ClojureScript 0.0-3308 {}
goog.provide('cljs_http.client');
goog.require('cljs.core');
goog.require('goog.Uri');
goog.require('cljs_http.core');
goog.require('cljs.core.async');
goog.require('no.en.core');
goog.require('cljs_http.util');
goog.require('clojure.string');
goog.require('cljs.reader');
cljs_http.client.if_pos = (function cljs_http$client$if_pos(v){
if(cljs.core.truth_((function (){var and__4309__auto__ = v;
if(cljs.core.truth_(and__4309__auto__)){
return (v > (0));
} else {
return and__4309__auto__;
}
})())){
return v;
} else {
return null;
}
});
/**
 * Parse `s` as query params and return a hash map.
 */
cljs_http.client.parse_query_params = (function cljs_http$client$parse_query_params(s){
if(cljs.core.not.call(null,clojure.string.blank_QMARK_.call(null,s))){
return cljs.core.reduce.call(null,(function (p1__11388_SHARP_,p2__11387_SHARP_){
var vec__11390 = clojure.string.split.call(null,p2__11387_SHARP_,/=/);
var k = cljs.core.nth.call(null,vec__11390,(0),null);
var v = cljs.core.nth.call(null,vec__11390,(1),null);
return cljs.core.assoc.call(null,p1__11388_SHARP_,cljs.core.keyword.call(null,no.en.core.url_decode.call(null,k)),no.en.core.url_decode.call(null,v));
}),cljs.core.PersistentArrayMap.EMPTY,clojure.string.split.call(null,[cljs.core.str(s)].join(''),/&/));
} else {
return null;
}
});
/**
 * Parse `url` into a hash map.
 */
cljs_http.client.parse_url = (function cljs_http$client$parse_url(url){
if(cljs.core.not.call(null,clojure.string.blank_QMARK_.call(null,url))){
var uri = goog.Uri.parse(url);
var query_data = uri.getQueryData();
return new cljs.core.PersistentArrayMap(null, 6, [new cljs.core.Keyword(null,"scheme","scheme",90199613),cljs.core.keyword.call(null,uri.getScheme()),new cljs.core.Keyword(null,"server-name","server-name",-1012104295),uri.getDomain(),new cljs.core.Keyword(null,"server-port","server-port",663745648),cljs_http.client.if_pos.call(null,uri.getPort()),new cljs.core.Keyword(null,"uri","uri",-774711847),uri.getPath(),new cljs.core.Keyword(null,"query-string","query-string",-1018845061),((cljs.core.not.call(null,query_data.isEmpty()))?[cljs.core.str(query_data)].join(''):null),new cljs.core.Keyword(null,"query-params","query-params",900640534),((cljs.core.not.call(null,query_data.isEmpty()))?cljs_http.client.parse_query_params.call(null,[cljs.core.str(query_data)].join('')):null)], null);
} else {
return null;
}
});
cljs_http.client.unexceptional_status_QMARK_ = new cljs.core.PersistentHashSet(null, new cljs.core.PersistentArrayMap(null, 13, [(205),null,(206),null,(300),null,(204),null,(307),null,(303),null,(301),null,(201),null,(302),null,(202),null,(200),null,(203),null,(207),null], null), null);
cljs_http.client.encode_val = (function cljs_http$client$encode_val(k,v){
return [cljs.core.str(no.en.core.url_encode.call(null,cljs.core.name.call(null,k))),cljs.core.str("="),cljs.core.str(no.en.core.url_encode.call(null,[cljs.core.str(v)].join('')))].join('');
});
cljs_http.client.encode_vals = (function cljs_http$client$encode_vals(k,vs){
return clojure.string.join.call(null,"&",cljs.core.map.call(null,(function (p1__11391_SHARP_){
return cljs_http.client.encode_val.call(null,k,p1__11391_SHARP_);
}),vs));
});
cljs_http.client.encode_param = (function cljs_http$client$encode_param(p__11392){
var vec__11394 = p__11392;
var k = cljs.core.nth.call(null,vec__11394,(0),null);
var v = cljs.core.nth.call(null,vec__11394,(1),null);
if(cljs.core.coll_QMARK_.call(null,v)){
return cljs_http.client.encode_vals.call(null,k,v);
} else {
return cljs_http.client.encode_val.call(null,k,v);
}
});
cljs_http.client.generate_query_string = (function cljs_http$client$generate_query_string(params){
return clojure.string.join.call(null,"&",cljs.core.map.call(null,cljs_http.client.encode_param,params));
});
cljs_http.client.regex_char_esc_smap = (function (){var esc_chars = "()*&^%$#!+";
return cljs.core.zipmap.call(null,esc_chars,cljs.core.map.call(null,((function (esc_chars){
return (function (p1__11395_SHARP_){
return [cljs.core.str("\\"),cljs.core.str(p1__11395_SHARP_)].join('');
});})(esc_chars))
,esc_chars));
})();
/**
 * Escape special characters -- for content-type.
 */
cljs_http.client.escape_special = (function cljs_http$client$escape_special(string){
return cljs.core.reduce.call(null,cljs.core.str,cljs.core.replace.call(null,cljs_http.client.regex_char_esc_smap,string));
});
/**
 * Decocde the :body of `response` with `decode-fn` if the content type matches.
 */
cljs_http.client.decode_body = (function cljs_http$client$decode_body(response,decode_fn,content_type,request_method){
if(cljs.core.truth_((function (){var and__4309__auto__ = cljs.core.not_EQ_.call(null,new cljs.core.Keyword(null,"head","head",-771383919),request_method);
if(and__4309__auto__){
var and__4309__auto____$1 = cljs.core.not_EQ_.call(null,(204),new cljs.core.Keyword(null,"status","status",-1997798413).cljs$core$IFn$_invoke$arity$1(response));
if(and__4309__auto____$1){
return cljs.core.re_find.call(null,cljs.core.re_pattern.call(null,[cljs.core.str("(?i)"),cljs.core.str(cljs_http.client.escape_special.call(null,content_type))].join('')),[cljs.core.str(cljs.core.get.call(null,new cljs.core.Keyword(null,"headers","headers",-835030129).cljs$core$IFn$_invoke$arity$1(response),"content-type",""))].join(''));
} else {
return and__4309__auto____$1;
}
} else {
return and__4309__auto__;
}
})())){
return cljs.core.update_in.call(null,response,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"body","body",-2049205669)], null),decode_fn);
} else {
return response;
}
});
/**
 * Encode :edn-params in the `request` :body and set the appropriate
 * Content Type header.
 */
cljs_http.client.wrap_edn_params = (function cljs_http$client$wrap_edn_params(client){
return (function (request){
var temp__4423__auto__ = new cljs.core.Keyword(null,"edn-params","edn-params",894273052).cljs$core$IFn$_invoke$arity$1(request);
if(cljs.core.truth_(temp__4423__auto__)){
var params = temp__4423__auto__;
return client.call(null,cljs.core.assoc_in.call(null,cljs.core.assoc.call(null,cljs.core.dissoc.call(null,request,new cljs.core.Keyword(null,"edn-params","edn-params",894273052)),new cljs.core.Keyword(null,"body","body",-2049205669),cljs.core.pr_str.call(null,params)),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"headers","headers",-835030129),"content-type"], null),"application/edn"));
} else {
return client.call(null,request);
}
});
});
/**
 * Decode application/edn responses.
 */
cljs_http.client.wrap_edn_response = (function cljs_http$client$wrap_edn_response(client){
return (function (request){
return cljs.core.async.map.call(null,(function (p1__11396_SHARP_){
return cljs_http.client.decode_body.call(null,p1__11396_SHARP_,cljs.reader.read_string,"application/edn",new cljs.core.Keyword(null,"request-method","request-method",1764796830).cljs$core$IFn$_invoke$arity$1(request));
}),new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [client.call(null,request)], null));
});
});
cljs_http.client.wrap_default_headers = (function cljs_http$client$wrap_default_headers(){
var argseq__5361__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return cljs_http.client.wrap_default_headers.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__5361__auto__);
});

cljs_http.client.wrap_default_headers.cljs$core$IFn$_invoke$arity$variadic = (function (client,p__11399){
var vec__11400 = p__11399;
var default_headers = cljs.core.nth.call(null,vec__11400,(0),null);
return ((function (vec__11400,default_headers){
return (function (request){
var temp__4423__auto__ = (function (){var or__4321__auto__ = new cljs.core.Keyword(null,"default-headers","default-headers",-43146094).cljs$core$IFn$_invoke$arity$1(request);
if(cljs.core.truth_(or__4321__auto__)){
return or__4321__auto__;
} else {
return default_headers;
}
})();
if(cljs.core.truth_(temp__4423__auto__)){
var default_headers__$1 = temp__4423__auto__;
return client.call(null,cljs.core.assoc.call(null,request,new cljs.core.Keyword(null,"default-headers","default-headers",-43146094),default_headers__$1));
} else {
return client.call(null,request);
}
});
;})(vec__11400,default_headers))
});

cljs_http.client.wrap_default_headers.cljs$lang$maxFixedArity = (1);

cljs_http.client.wrap_default_headers.cljs$lang$applyTo = (function (seq11397){
var G__11398 = cljs.core.first.call(null,seq11397);
var seq11397__$1 = cljs.core.next.call(null,seq11397);
return cljs_http.client.wrap_default_headers.cljs$core$IFn$_invoke$arity$variadic(G__11398,seq11397__$1);
});
cljs_http.client.wrap_accept = (function cljs_http$client$wrap_accept(){
var argseq__5361__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return cljs_http.client.wrap_accept.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__5361__auto__);
});

cljs_http.client.wrap_accept.cljs$core$IFn$_invoke$arity$variadic = (function (client,p__11403){
var vec__11404 = p__11403;
var accept = cljs.core.nth.call(null,vec__11404,(0),null);
return ((function (vec__11404,accept){
return (function (request){
var temp__4423__auto__ = (function (){var or__4321__auto__ = new cljs.core.Keyword(null,"accept","accept",1874130431).cljs$core$IFn$_invoke$arity$1(request);
if(cljs.core.truth_(or__4321__auto__)){
return or__4321__auto__;
} else {
return accept;
}
})();
if(cljs.core.truth_(temp__4423__auto__)){
var accept__$1 = temp__4423__auto__;
return client.call(null,cljs.core.assoc_in.call(null,request,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"headers","headers",-835030129),"accept"], null),accept__$1));
} else {
return client.call(null,request);
}
});
;})(vec__11404,accept))
});

cljs_http.client.wrap_accept.cljs$lang$maxFixedArity = (1);

cljs_http.client.wrap_accept.cljs$lang$applyTo = (function (seq11401){
var G__11402 = cljs.core.first.call(null,seq11401);
var seq11401__$1 = cljs.core.next.call(null,seq11401);
return cljs_http.client.wrap_accept.cljs$core$IFn$_invoke$arity$variadic(G__11402,seq11401__$1);
});
cljs_http.client.wrap_content_type = (function cljs_http$client$wrap_content_type(){
var argseq__5361__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return cljs_http.client.wrap_content_type.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__5361__auto__);
});

cljs_http.client.wrap_content_type.cljs$core$IFn$_invoke$arity$variadic = (function (client,p__11407){
var vec__11408 = p__11407;
var content_type = cljs.core.nth.call(null,vec__11408,(0),null);
return ((function (vec__11408,content_type){
return (function (request){
var temp__4423__auto__ = (function (){var or__4321__auto__ = new cljs.core.Keyword(null,"content-type","content-type",-508222634).cljs$core$IFn$_invoke$arity$1(request);
if(cljs.core.truth_(or__4321__auto__)){
return or__4321__auto__;
} else {
return content_type;
}
})();
if(cljs.core.truth_(temp__4423__auto__)){
var content_type__$1 = temp__4423__auto__;
return client.call(null,cljs.core.assoc_in.call(null,request,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"headers","headers",-835030129),"content-type"], null),content_type__$1));
} else {
return client.call(null,request);
}
});
;})(vec__11408,content_type))
});

cljs_http.client.wrap_content_type.cljs$lang$maxFixedArity = (1);

cljs_http.client.wrap_content_type.cljs$lang$applyTo = (function (seq11405){
var G__11406 = cljs.core.first.call(null,seq11405);
var seq11405__$1 = cljs.core.next.call(null,seq11405);
return cljs_http.client.wrap_content_type.cljs$core$IFn$_invoke$arity$variadic(G__11406,seq11405__$1);
});
cljs_http.client.default_transit_opts = new cljs.core.PersistentArrayMap(null, 4, [new cljs.core.Keyword(null,"encoding","encoding",1728578272),new cljs.core.Keyword(null,"json","json",1279968570),new cljs.core.Keyword(null,"encoding-opts","encoding-opts",-1805664631),cljs.core.PersistentArrayMap.EMPTY,new cljs.core.Keyword(null,"decoding","decoding",-568180903),new cljs.core.Keyword(null,"json","json",1279968570),new cljs.core.Keyword(null,"decoding-opts","decoding-opts",1050289140),cljs.core.PersistentArrayMap.EMPTY], null);
/**
 * Encode :transit-params in the `request` :body and set the appropriate
 * Content Type header.
 * 
 * A :transit-opts map can be optionally provided with the following keys:
 * 
 * :encoding                #{:json, :json-verbose}
 * :decoding                #{:json, :json-verbose}
 * :encoding/decoding-opts  appropriate map of options to be passed to
 * transit writer/reader, respectively.
 */
cljs_http.client.wrap_transit_params = (function cljs_http$client$wrap_transit_params(client){
return (function (request){
var temp__4423__auto__ = new cljs.core.Keyword(null,"transit-params","transit-params",357261095).cljs$core$IFn$_invoke$arity$1(request);
if(cljs.core.truth_(temp__4423__auto__)){
var params = temp__4423__auto__;
var map__11410 = cljs.core.merge.call(null,cljs_http.client.default_transit_opts,new cljs.core.Keyword(null,"transit-opts","transit-opts",1104386010).cljs$core$IFn$_invoke$arity$1(request));
var map__11410__$1 = ((cljs.core.seq_QMARK_.call(null,map__11410))?cljs.core.apply.call(null,cljs.core.hash_map,map__11410):map__11410);
var encoding = cljs.core.get.call(null,map__11410__$1,new cljs.core.Keyword(null,"encoding","encoding",1728578272));
var encoding_opts = cljs.core.get.call(null,map__11410__$1,new cljs.core.Keyword(null,"encoding-opts","encoding-opts",-1805664631));
return client.call(null,cljs.core.assoc_in.call(null,cljs.core.assoc.call(null,cljs.core.dissoc.call(null,request,new cljs.core.Keyword(null,"transit-params","transit-params",357261095)),new cljs.core.Keyword(null,"body","body",-2049205669),cljs_http.util.transit_encode.call(null,params,encoding,encoding_opts)),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"headers","headers",-835030129),"content-type"], null),"application/transit+json"));
} else {
return client.call(null,request);
}
});
});
/**
 * Decode application/transit+json responses.
 */
cljs_http.client.wrap_transit_response = (function cljs_http$client$wrap_transit_response(client){
return (function (request){
var map__11414 = cljs.core.merge.call(null,cljs_http.client.default_transit_opts,new cljs.core.Keyword(null,"transit-opts","transit-opts",1104386010).cljs$core$IFn$_invoke$arity$1(request));
var map__11414__$1 = ((cljs.core.seq_QMARK_.call(null,map__11414))?cljs.core.apply.call(null,cljs.core.hash_map,map__11414):map__11414);
var decoding = cljs.core.get.call(null,map__11414__$1,new cljs.core.Keyword(null,"decoding","decoding",-568180903));
var decoding_opts = cljs.core.get.call(null,map__11414__$1,new cljs.core.Keyword(null,"decoding-opts","decoding-opts",1050289140));
var transit_decode = ((function (map__11414,map__11414__$1,decoding,decoding_opts){
return (function (p1__11411_SHARP_){
return cljs_http.util.transit_decode.call(null,p1__11411_SHARP_,decoding,decoding_opts);
});})(map__11414,map__11414__$1,decoding,decoding_opts))
;
return cljs.core.async.map.call(null,((function (map__11414,map__11414__$1,decoding,decoding_opts,transit_decode){
return (function (p1__11412_SHARP_){
return cljs_http.client.decode_body.call(null,p1__11412_SHARP_,transit_decode,"application/transit+json",new cljs.core.Keyword(null,"request-method","request-method",1764796830).cljs$core$IFn$_invoke$arity$1(request));
});})(map__11414,map__11414__$1,decoding,decoding_opts,transit_decode))
,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [client.call(null,request)], null));
});
});
/**
 * Encode :json-params in the `request` :body and set the appropriate
 * Content Type header.
 */
cljs_http.client.wrap_json_params = (function cljs_http$client$wrap_json_params(client){
return (function (request){
var temp__4423__auto__ = new cljs.core.Keyword(null,"json-params","json-params",-1112693596).cljs$core$IFn$_invoke$arity$1(request);
if(cljs.core.truth_(temp__4423__auto__)){
var params = temp__4423__auto__;
return client.call(null,cljs.core.assoc_in.call(null,cljs.core.assoc.call(null,cljs.core.dissoc.call(null,request,new cljs.core.Keyword(null,"json-params","json-params",-1112693596)),new cljs.core.Keyword(null,"body","body",-2049205669),cljs_http.util.json_encode.call(null,params)),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"headers","headers",-835030129),"content-type"], null),"application/json"));
} else {
return client.call(null,request);
}
});
});
/**
 * Decode application/json responses.
 */
cljs_http.client.wrap_json_response = (function cljs_http$client$wrap_json_response(client){
return (function (request){
return cljs.core.async.map.call(null,(function (p1__11415_SHARP_){
return cljs_http.client.decode_body.call(null,p1__11415_SHARP_,cljs_http.util.json_decode,"application/json",new cljs.core.Keyword(null,"request-method","request-method",1764796830).cljs$core$IFn$_invoke$arity$1(request));
}),new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [client.call(null,request)], null));
});
});
cljs_http.client.wrap_query_params = (function cljs_http$client$wrap_query_params(client){
return (function (p__11418){
var map__11419 = p__11418;
var map__11419__$1 = ((cljs.core.seq_QMARK_.call(null,map__11419))?cljs.core.apply.call(null,cljs.core.hash_map,map__11419):map__11419);
var req = map__11419__$1;
var query_params = cljs.core.get.call(null,map__11419__$1,new cljs.core.Keyword(null,"query-params","query-params",900640534));
if(cljs.core.truth_(query_params)){
return client.call(null,cljs.core.assoc.call(null,cljs.core.dissoc.call(null,req,new cljs.core.Keyword(null,"query-params","query-params",900640534)),new cljs.core.Keyword(null,"query-string","query-string",-1018845061),cljs_http.client.generate_query_string.call(null,query_params)));
} else {
return client.call(null,req);
}
});
});
cljs_http.client.wrap_form_params = (function cljs_http$client$wrap_form_params(client){
return (function (p__11422){
var map__11423 = p__11422;
var map__11423__$1 = ((cljs.core.seq_QMARK_.call(null,map__11423))?cljs.core.apply.call(null,cljs.core.hash_map,map__11423):map__11423);
var request = map__11423__$1;
var form_params = cljs.core.get.call(null,map__11423__$1,new cljs.core.Keyword(null,"form-params","form-params",1884296467));
var request_method = cljs.core.get.call(null,map__11423__$1,new cljs.core.Keyword(null,"request-method","request-method",1764796830));
if(cljs.core.truth_((function (){var and__4309__auto__ = form_params;
if(cljs.core.truth_(and__4309__auto__)){
return new cljs.core.PersistentHashSet(null, new cljs.core.PersistentArrayMap(null, 4, [new cljs.core.Keyword(null,"patch","patch",380775109),null,new cljs.core.Keyword(null,"delete","delete",-1768633620),null,new cljs.core.Keyword(null,"post","post",269697687),null,new cljs.core.Keyword(null,"put","put",1299772570),null], null), null).call(null,request_method);
} else {
return and__4309__auto__;
}
})())){
return client.call(null,cljs.core.assoc_in.call(null,cljs.core.assoc.call(null,cljs.core.dissoc.call(null,request,new cljs.core.Keyword(null,"form-params","form-params",1884296467)),new cljs.core.Keyword(null,"body","body",-2049205669),cljs_http.client.generate_query_string.call(null,form_params)),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"headers","headers",-835030129),"content-type"], null),"application/x-www-form-urlencoded"));
} else {
return client.call(null,request);
}
});
});
cljs_http.client.generate_form_data = (function cljs_http$client$generate_form_data(params){
var form_data = (new FormData());
var seq__11430_11436 = cljs.core.seq.call(null,params);
var chunk__11431_11437 = null;
var count__11432_11438 = (0);
var i__11433_11439 = (0);
while(true){
if((i__11433_11439 < count__11432_11438)){
var vec__11434_11440 = cljs.core._nth.call(null,chunk__11431_11437,i__11433_11439);
var k_11441 = cljs.core.nth.call(null,vec__11434_11440,(0),null);
var v_11442 = cljs.core.nth.call(null,vec__11434_11440,(1),null);
form_data.append(cljs.core.name.call(null,k_11441),v_11442);

var G__11443 = seq__11430_11436;
var G__11444 = chunk__11431_11437;
var G__11445 = count__11432_11438;
var G__11446 = (i__11433_11439 + (1));
seq__11430_11436 = G__11443;
chunk__11431_11437 = G__11444;
count__11432_11438 = G__11445;
i__11433_11439 = G__11446;
continue;
} else {
var temp__4425__auto___11447 = cljs.core.seq.call(null,seq__11430_11436);
if(temp__4425__auto___11447){
var seq__11430_11448__$1 = temp__4425__auto___11447;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__11430_11448__$1)){
var c__5106__auto___11449 = cljs.core.chunk_first.call(null,seq__11430_11448__$1);
var G__11450 = cljs.core.chunk_rest.call(null,seq__11430_11448__$1);
var G__11451 = c__5106__auto___11449;
var G__11452 = cljs.core.count.call(null,c__5106__auto___11449);
var G__11453 = (0);
seq__11430_11436 = G__11450;
chunk__11431_11437 = G__11451;
count__11432_11438 = G__11452;
i__11433_11439 = G__11453;
continue;
} else {
var vec__11435_11454 = cljs.core.first.call(null,seq__11430_11448__$1);
var k_11455 = cljs.core.nth.call(null,vec__11435_11454,(0),null);
var v_11456 = cljs.core.nth.call(null,vec__11435_11454,(1),null);
form_data.append(cljs.core.name.call(null,k_11455),v_11456);

var G__11457 = cljs.core.next.call(null,seq__11430_11448__$1);
var G__11458 = null;
var G__11459 = (0);
var G__11460 = (0);
seq__11430_11436 = G__11457;
chunk__11431_11437 = G__11458;
count__11432_11438 = G__11459;
i__11433_11439 = G__11460;
continue;
}
} else {
}
}
break;
}

return form_data;
});
cljs_http.client.wrap_multipart_params = (function cljs_http$client$wrap_multipart_params(client){
return (function (p__11463){
var map__11464 = p__11463;
var map__11464__$1 = ((cljs.core.seq_QMARK_.call(null,map__11464))?cljs.core.apply.call(null,cljs.core.hash_map,map__11464):map__11464);
var request = map__11464__$1;
var multipart_params = cljs.core.get.call(null,map__11464__$1,new cljs.core.Keyword(null,"multipart-params","multipart-params",-1033508707));
var request_method = cljs.core.get.call(null,map__11464__$1,new cljs.core.Keyword(null,"request-method","request-method",1764796830));
if(cljs.core.truth_((function (){var and__4309__auto__ = multipart_params;
if(cljs.core.truth_(and__4309__auto__)){
return new cljs.core.PersistentHashSet(null, new cljs.core.PersistentArrayMap(null, 4, [new cljs.core.Keyword(null,"patch","patch",380775109),null,new cljs.core.Keyword(null,"delete","delete",-1768633620),null,new cljs.core.Keyword(null,"post","post",269697687),null,new cljs.core.Keyword(null,"put","put",1299772570),null], null), null).call(null,request_method);
} else {
return and__4309__auto__;
}
})())){
return client.call(null,cljs.core.assoc.call(null,cljs.core.dissoc.call(null,request,new cljs.core.Keyword(null,"multipart-params","multipart-params",-1033508707)),new cljs.core.Keyword(null,"body","body",-2049205669),cljs_http.client.generate_form_data.call(null,multipart_params)));
} else {
return client.call(null,request);
}
});
});
cljs_http.client.wrap_method = (function cljs_http$client$wrap_method(client){
return (function (req){
var temp__4423__auto__ = new cljs.core.Keyword(null,"method","method",55703592).cljs$core$IFn$_invoke$arity$1(req);
if(cljs.core.truth_(temp__4423__auto__)){
var m = temp__4423__auto__;
return client.call(null,cljs.core.assoc.call(null,cljs.core.dissoc.call(null,req,new cljs.core.Keyword(null,"method","method",55703592)),new cljs.core.Keyword(null,"request-method","request-method",1764796830),m));
} else {
return client.call(null,req);
}
});
});
cljs_http.client.wrap_server_name = (function cljs_http$client$wrap_server_name(client,server_name){
return (function (p1__11465_SHARP_){
return client.call(null,cljs.core.assoc.call(null,p1__11465_SHARP_,new cljs.core.Keyword(null,"server-name","server-name",-1012104295),server_name));
});
});
cljs_http.client.wrap_url = (function cljs_http$client$wrap_url(client){
return (function (p__11469){
var map__11470 = p__11469;
var map__11470__$1 = ((cljs.core.seq_QMARK_.call(null,map__11470))?cljs.core.apply.call(null,cljs.core.hash_map,map__11470):map__11470);
var req = map__11470__$1;
var query_params = cljs.core.get.call(null,map__11470__$1,new cljs.core.Keyword(null,"query-params","query-params",900640534));
var temp__4423__auto__ = cljs_http.client.parse_url.call(null,new cljs.core.Keyword(null,"url","url",276297046).cljs$core$IFn$_invoke$arity$1(req));
if(cljs.core.truth_(temp__4423__auto__)){
var spec = temp__4423__auto__;
return client.call(null,cljs.core.update_in.call(null,cljs.core.dissoc.call(null,cljs.core.merge.call(null,req,spec),new cljs.core.Keyword(null,"url","url",276297046)),new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"query-params","query-params",900640534)], null),((function (spec,temp__4423__auto__,map__11470,map__11470__$1,req,query_params){
return (function (p1__11466_SHARP_){
return cljs.core.merge.call(null,p1__11466_SHARP_,query_params);
});})(spec,temp__4423__auto__,map__11470,map__11470__$1,req,query_params))
));
} else {
return client.call(null,req);
}
});
});
/**
 * Middleware converting the :basic-auth option or `credentials` into
 * an Authorization header.
 */
cljs_http.client.wrap_basic_auth = (function cljs_http$client$wrap_basic_auth(){
var argseq__5361__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return cljs_http.client.wrap_basic_auth.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__5361__auto__);
});

cljs_http.client.wrap_basic_auth.cljs$core$IFn$_invoke$arity$variadic = (function (client,p__11473){
var vec__11474 = p__11473;
var credentials = cljs.core.nth.call(null,vec__11474,(0),null);
return ((function (vec__11474,credentials){
return (function (req){
var credentials__$1 = (function (){var or__4321__auto__ = new cljs.core.Keyword(null,"basic-auth","basic-auth",-673163332).cljs$core$IFn$_invoke$arity$1(req);
if(cljs.core.truth_(or__4321__auto__)){
return or__4321__auto__;
} else {
return credentials;
}
})();
if(!(cljs.core.empty_QMARK_.call(null,credentials__$1))){
return client.call(null,cljs.core.assoc_in.call(null,cljs.core.dissoc.call(null,req,new cljs.core.Keyword(null,"basic-auth","basic-auth",-673163332)),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"headers","headers",-835030129),"authorization"], null),cljs_http.util.basic_auth.call(null,credentials__$1)));
} else {
return client.call(null,req);
}
});
;})(vec__11474,credentials))
});

cljs_http.client.wrap_basic_auth.cljs$lang$maxFixedArity = (1);

cljs_http.client.wrap_basic_auth.cljs$lang$applyTo = (function (seq11471){
var G__11472 = cljs.core.first.call(null,seq11471);
var seq11471__$1 = cljs.core.next.call(null,seq11471);
return cljs_http.client.wrap_basic_auth.cljs$core$IFn$_invoke$arity$variadic(G__11472,seq11471__$1);
});
/**
 * Middleware converting the :oauth-token option into an Authorization header.
 */
cljs_http.client.wrap_oauth = (function cljs_http$client$wrap_oauth(client){
return (function (req){
var temp__4423__auto__ = new cljs.core.Keyword(null,"oauth-token","oauth-token",311415191).cljs$core$IFn$_invoke$arity$1(req);
if(cljs.core.truth_(temp__4423__auto__)){
var oauth_token = temp__4423__auto__;
return client.call(null,cljs.core.assoc_in.call(null,cljs.core.dissoc.call(null,req,new cljs.core.Keyword(null,"oauth-token","oauth-token",311415191)),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"headers","headers",-835030129),"authorization"], null),[cljs.core.str("Bearer "),cljs.core.str(oauth_token)].join('')));
} else {
return client.call(null,req);
}
});
});
/**
 * Pipe the response-channel into the request-map's
 * custom channel (e.g. to enable transducers)
 */
cljs_http.client.wrap_channel_from_request_map = (function cljs_http$client$wrap_channel_from_request_map(client){
return (function (request){
var temp__4423__auto__ = new cljs.core.Keyword(null,"channel","channel",734187692).cljs$core$IFn$_invoke$arity$1(request);
if(cljs.core.truth_(temp__4423__auto__)){
var custom_channel = temp__4423__auto__;
return cljs.core.async.pipe.call(null,client.call(null,request),custom_channel);
} else {
return client.call(null,request);
}
});
});
/**
 * Returns a batteries-included HTTP request function coresponding to the given
 * core client. See client/request
 */
cljs_http.client.wrap_request = (function cljs_http$client$wrap_request(request){
return cljs_http.client.wrap_default_headers.call(null,cljs_http.client.wrap_channel_from_request_map.call(null,cljs_http.client.wrap_url.call(null,cljs_http.client.wrap_method.call(null,cljs_http.client.wrap_oauth.call(null,cljs_http.client.wrap_basic_auth.call(null,cljs_http.client.wrap_query_params.call(null,cljs_http.client.wrap_content_type.call(null,cljs_http.client.wrap_json_response.call(null,cljs_http.client.wrap_json_params.call(null,cljs_http.client.wrap_transit_response.call(null,cljs_http.client.wrap_transit_params.call(null,cljs_http.client.wrap_edn_response.call(null,cljs_http.client.wrap_edn_params.call(null,cljs_http.client.wrap_multipart_params.call(null,cljs_http.client.wrap_form_params.call(null,cljs_http.client.wrap_accept.call(null,request)))))))))))))))));
});
/**
 * Executes the HTTP request corresponding to the given map and returns the
 * response map for corresponding to the resulting HTTP response.
 * 
 * In addition to the standard Ring request keys, the following keys are also
 * recognized:
 * * :url
 * * :method
 * * :query-params
 */
cljs_http.client.request = cljs_http.client.wrap_request.call(null,cljs_http.core.request);
/**
 * Like #'request, but sets the :method and :url as appropriate.
 */
cljs_http.client.delete$ = (function cljs_http$client$delete(){
var argseq__5361__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return cljs_http.client.delete$.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__5361__auto__);
});

cljs_http.client.delete$.cljs$core$IFn$_invoke$arity$variadic = (function (url,p__11477){
var vec__11478 = p__11477;
var req = cljs.core.nth.call(null,vec__11478,(0),null);
return cljs_http.client.request.call(null,cljs.core.merge.call(null,req,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"method","method",55703592),new cljs.core.Keyword(null,"delete","delete",-1768633620),new cljs.core.Keyword(null,"url","url",276297046),url], null)));
});

cljs_http.client.delete$.cljs$lang$maxFixedArity = (1);

cljs_http.client.delete$.cljs$lang$applyTo = (function (seq11475){
var G__11476 = cljs.core.first.call(null,seq11475);
var seq11475__$1 = cljs.core.next.call(null,seq11475);
return cljs_http.client.delete$.cljs$core$IFn$_invoke$arity$variadic(G__11476,seq11475__$1);
});
/**
 * Like #'request, but sets the :method and :url as appropriate.
 */
cljs_http.client.get = (function cljs_http$client$get(){
var argseq__5361__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return cljs_http.client.get.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__5361__auto__);
});

cljs_http.client.get.cljs$core$IFn$_invoke$arity$variadic = (function (url,p__11481){
var vec__11482 = p__11481;
var req = cljs.core.nth.call(null,vec__11482,(0),null);
return cljs_http.client.request.call(null,cljs.core.merge.call(null,req,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"method","method",55703592),new cljs.core.Keyword(null,"get","get",1683182755),new cljs.core.Keyword(null,"url","url",276297046),url], null)));
});

cljs_http.client.get.cljs$lang$maxFixedArity = (1);

cljs_http.client.get.cljs$lang$applyTo = (function (seq11479){
var G__11480 = cljs.core.first.call(null,seq11479);
var seq11479__$1 = cljs.core.next.call(null,seq11479);
return cljs_http.client.get.cljs$core$IFn$_invoke$arity$variadic(G__11480,seq11479__$1);
});
/**
 * Like #'request, but sets the :method and :url as appropriate.
 */
cljs_http.client.head = (function cljs_http$client$head(){
var argseq__5361__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return cljs_http.client.head.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__5361__auto__);
});

cljs_http.client.head.cljs$core$IFn$_invoke$arity$variadic = (function (url,p__11485){
var vec__11486 = p__11485;
var req = cljs.core.nth.call(null,vec__11486,(0),null);
return cljs_http.client.request.call(null,cljs.core.merge.call(null,req,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"method","method",55703592),new cljs.core.Keyword(null,"head","head",-771383919),new cljs.core.Keyword(null,"url","url",276297046),url], null)));
});

cljs_http.client.head.cljs$lang$maxFixedArity = (1);

cljs_http.client.head.cljs$lang$applyTo = (function (seq11483){
var G__11484 = cljs.core.first.call(null,seq11483);
var seq11483__$1 = cljs.core.next.call(null,seq11483);
return cljs_http.client.head.cljs$core$IFn$_invoke$arity$variadic(G__11484,seq11483__$1);
});
/**
 * Like #'request, but sets the :method and :url as appropriate.
 */
cljs_http.client.jsonp = (function cljs_http$client$jsonp(){
var argseq__5361__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return cljs_http.client.jsonp.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__5361__auto__);
});

cljs_http.client.jsonp.cljs$core$IFn$_invoke$arity$variadic = (function (url,p__11489){
var vec__11490 = p__11489;
var req = cljs.core.nth.call(null,vec__11490,(0),null);
return cljs_http.client.request.call(null,cljs.core.merge.call(null,req,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"method","method",55703592),new cljs.core.Keyword(null,"jsonp","jsonp",226119588),new cljs.core.Keyword(null,"url","url",276297046),url], null)));
});

cljs_http.client.jsonp.cljs$lang$maxFixedArity = (1);

cljs_http.client.jsonp.cljs$lang$applyTo = (function (seq11487){
var G__11488 = cljs.core.first.call(null,seq11487);
var seq11487__$1 = cljs.core.next.call(null,seq11487);
return cljs_http.client.jsonp.cljs$core$IFn$_invoke$arity$variadic(G__11488,seq11487__$1);
});
/**
 * Like #'request, but sets the :method and :url as appropriate.
 */
cljs_http.client.move = (function cljs_http$client$move(){
var argseq__5361__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return cljs_http.client.move.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__5361__auto__);
});

cljs_http.client.move.cljs$core$IFn$_invoke$arity$variadic = (function (url,p__11493){
var vec__11494 = p__11493;
var req = cljs.core.nth.call(null,vec__11494,(0),null);
return cljs_http.client.request.call(null,cljs.core.merge.call(null,req,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"method","method",55703592),new cljs.core.Keyword(null,"move","move",-2110884309),new cljs.core.Keyword(null,"url","url",276297046),url], null)));
});

cljs_http.client.move.cljs$lang$maxFixedArity = (1);

cljs_http.client.move.cljs$lang$applyTo = (function (seq11491){
var G__11492 = cljs.core.first.call(null,seq11491);
var seq11491__$1 = cljs.core.next.call(null,seq11491);
return cljs_http.client.move.cljs$core$IFn$_invoke$arity$variadic(G__11492,seq11491__$1);
});
/**
 * Like #'request, but sets the :method and :url as appropriate.
 */
cljs_http.client.options = (function cljs_http$client$options(){
var argseq__5361__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return cljs_http.client.options.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__5361__auto__);
});

cljs_http.client.options.cljs$core$IFn$_invoke$arity$variadic = (function (url,p__11497){
var vec__11498 = p__11497;
var req = cljs.core.nth.call(null,vec__11498,(0),null);
return cljs_http.client.request.call(null,cljs.core.merge.call(null,req,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"method","method",55703592),new cljs.core.Keyword(null,"options","options",99638489),new cljs.core.Keyword(null,"url","url",276297046),url], null)));
});

cljs_http.client.options.cljs$lang$maxFixedArity = (1);

cljs_http.client.options.cljs$lang$applyTo = (function (seq11495){
var G__11496 = cljs.core.first.call(null,seq11495);
var seq11495__$1 = cljs.core.next.call(null,seq11495);
return cljs_http.client.options.cljs$core$IFn$_invoke$arity$variadic(G__11496,seq11495__$1);
});
/**
 * Like #'request, but sets the :method and :url as appropriate.
 */
cljs_http.client.patch = (function cljs_http$client$patch(){
var argseq__5361__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return cljs_http.client.patch.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__5361__auto__);
});

cljs_http.client.patch.cljs$core$IFn$_invoke$arity$variadic = (function (url,p__11501){
var vec__11502 = p__11501;
var req = cljs.core.nth.call(null,vec__11502,(0),null);
return cljs_http.client.request.call(null,cljs.core.merge.call(null,req,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"method","method",55703592),new cljs.core.Keyword(null,"patch","patch",380775109),new cljs.core.Keyword(null,"url","url",276297046),url], null)));
});

cljs_http.client.patch.cljs$lang$maxFixedArity = (1);

cljs_http.client.patch.cljs$lang$applyTo = (function (seq11499){
var G__11500 = cljs.core.first.call(null,seq11499);
var seq11499__$1 = cljs.core.next.call(null,seq11499);
return cljs_http.client.patch.cljs$core$IFn$_invoke$arity$variadic(G__11500,seq11499__$1);
});
/**
 * Like #'request, but sets the :method and :url as appropriate.
 */
cljs_http.client.post = (function cljs_http$client$post(){
var argseq__5361__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return cljs_http.client.post.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__5361__auto__);
});

cljs_http.client.post.cljs$core$IFn$_invoke$arity$variadic = (function (url,p__11505){
var vec__11506 = p__11505;
var req = cljs.core.nth.call(null,vec__11506,(0),null);
return cljs_http.client.request.call(null,cljs.core.merge.call(null,req,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"method","method",55703592),new cljs.core.Keyword(null,"post","post",269697687),new cljs.core.Keyword(null,"url","url",276297046),url], null)));
});

cljs_http.client.post.cljs$lang$maxFixedArity = (1);

cljs_http.client.post.cljs$lang$applyTo = (function (seq11503){
var G__11504 = cljs.core.first.call(null,seq11503);
var seq11503__$1 = cljs.core.next.call(null,seq11503);
return cljs_http.client.post.cljs$core$IFn$_invoke$arity$variadic(G__11504,seq11503__$1);
});
/**
 * Like #'request, but sets the :method and :url as appropriate.
 */
cljs_http.client.put = (function cljs_http$client$put(){
var argseq__5361__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return cljs_http.client.put.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__5361__auto__);
});

cljs_http.client.put.cljs$core$IFn$_invoke$arity$variadic = (function (url,p__11509){
var vec__11510 = p__11509;
var req = cljs.core.nth.call(null,vec__11510,(0),null);
return cljs_http.client.request.call(null,cljs.core.merge.call(null,req,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"method","method",55703592),new cljs.core.Keyword(null,"put","put",1299772570),new cljs.core.Keyword(null,"url","url",276297046),url], null)));
});

cljs_http.client.put.cljs$lang$maxFixedArity = (1);

cljs_http.client.put.cljs$lang$applyTo = (function (seq11507){
var G__11508 = cljs.core.first.call(null,seq11507);
var seq11507__$1 = cljs.core.next.call(null,seq11507);
return cljs_http.client.put.cljs$core$IFn$_invoke$arity$variadic(G__11508,seq11507__$1);
});

//# sourceMappingURL=client.js.map