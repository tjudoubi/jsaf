window.Smartracker=function(){function i(a){var b=a.type;if("undefined"!=typeof b)return"text"==b&&(b=a.getAttribute("type")||"text"),b.toUpperCase()}function q(a,b){for(var c=0,d=a.length;c<d;c++){var e=null;switch(a[c].tagName.toUpperCase()){case "A":case "AREA":e="link";break;case "IMG":"A"!==a[c].parentNode.tagName.toUpperCase()&&(e="image");break;case "INPUT":switch(i(a[c])){case "SUBMIT":case "BUTTON":case "RESET":case "IMAGE":e="button";break;case "HIDDEN":break;default:e="input"}break;case "BUTTON":e=
"button";break;case "TEXTAREA":e="input";break;case "SELECT":e="input"}b(a[c],e)}}function r(a,b){var c=j,d;a:{d=a.parentNode;var e,g;do{if(k.hasAttr(d,"id")&&(g=d.getAttribute("id")||d.id)){d=h(g.split("-"));break a}if(k.hasAttr(d,"class")&&(e=l(d.getAttribute("class")||d.className||""))){d=e;break a}}while(d=d.parentNode);d="global"}c[0]=d;switch(b){case "link":j[1]=y(a);break;case "image":var c=j,f,i,n;d=(f=a.getAttribute("id")||a.id)?f:(n=l(a.getAttribute("class")||a.className||""))||(n=s(a.getAttribute("src")||
a.src))?n:(i=o(a.getAttribute("alt")||a.alt||a.getAttribute("title")||a.title))?i:"";c[1]=d;break;case "input":case "button":j[1]=z(a);break;default:return}f=j.join(t).replace(A,"");m.hasOwnProperty(f)&&(f=f+"T"+ ++m[f]);a.setAttribute(p,f);a.setAttribute("smartracker","on");m[f]=0;return f}function h(a){if(!a||!a.length)return"";for(var b=1,c=a.length;b<c;b++)a[b]=a[b].charAt(0).toUpperCase()+a[b].substring(1);return a.join("")}function s(a){var b=[],a=u(a),c=a.pathname.replace(/^\//,"").split("/"),
d=u(v.href);d.pathname.replace(/^\//,"").split("/");b.push(a.domainName||d.domainName);c[c.length-1]=c[c.length-1].split(".")[0]||"index";b.push.apply(b,c);return h(b)}function y(a){var b,c;if(b=a.getAttribute("id")||a.id)return h(b.split("-"));if(b=l(a.getAttribute("class")||a.className||""))return b;b=a.getAttribute("href")||a.href||"";!b||b.indexOf("#");if(b=s(b))switch(b.protocol){case "http:":case "https:":return b}return(c=o(k.innerText(a)))?c:"link"}function z(a){var b,c=[];if(b=a.getAttribute("id")||
a.id||a.getAttribute("name")||a.name||"")return h(b.split("-"));if(b=l(a.getAttribute("class")||a.className||""))return b;switch(i(a)){case "BUTTON":case "SUBMIT":case "RESET":case "IMAGE":return c.push("btn"),c.push(o(k.innerText(a))),h(c);case "HIDDEN":return"";default:return c.push("ipt"),c.push(a.id||a.name||""),h(c)}}function o(a){if(!a)return"";if(B.test(a))return a.replace(C,t);for(var b in SMARTRACKER_WORDS)if(SMARTRACKER_WORDS.hasOwnProperty(b)&&0<=a.indexOf(b))return SMARTRACKER_WORDS[b];
return""}function l(a){if(!a)return"";for(var a=a.split(" "),b=0,c=a.length;b<c;b++)if(!(0===a[b].indexOf("ui-")||0===a[b].indexOf("fn-")||0===a[b].indexOf("sl-")))return h(a[b].split("-"));return""}function u(a){var b=document.createElement("a");b.setAttribute("href",a);var a=b.pathname.split("/").slice(-1).join(""),c=a.split(".").slice(0,1).join(""),d=b.hostname,e=d.split(".").slice(0,1).join("");return{protocol:b.protocol,domain:d,domainName:e,path:b.pathname,pathname:b.pathname,file:a,fileName:c}}
var w=new Date,k={hasAttr:function(a,b){if(!a||1!=a.nodeType)return!1;if(a.hasAttribute)return a.hasAttribute(b);if("class"==b)return""!==a.className;if("style"==b)return""!==a.style.cssText;var c=a.getAttribute(b);return null==c?!1:"function"==typeof c?0==c.toString().indexOf("function "+b+"()"):!0},innerText:function(a){return a.innerText||a.textContent||""}},v=location,g=v.hostname,p="seed",t="-",B=/^[a-zA-Z][a-zA-Z0-9_\s-]*$/,C=/\s+/g,m={},D="file:"==location.protocol||"#debug"==location.hash||
!1,E=document.getElementsByTagName("*");"undefined"==typeof window.SMARTRACKER_WORDS&&(window.SMARTRACKER_WORDS={});"alipay.com"===g||".alipay.com"===g.substr(g.length-11)||".sit.alipay.net"===g.substr(g.length-15)||g.substr(g.length-18);var x=[];q(E,function(a,b){if(k.hasAttr(a,p)){var c=a.getAttribute(p)||a.seed;c&&(m[c]=0)}else b&&x.push(a)});var j=["",""],A=/[\\\.~!@#\$%\^&:;,\/\+\(\)\[\]\{\}]/g;q(x,r);D&&(window.status=new Date-w,window.console&&console.log&&console.log("Speed:",new Date-w,"ms"));
return{get:function(a){return r(a)}}}();