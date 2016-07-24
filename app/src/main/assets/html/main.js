window.onload = function() {
//	apps = document.getElementsByName("app");
//	apps[0].style.backgroundColor="#ff7070";
}

var index = 0;
var apps = [];

FourBeat.setButtonEventListener(function(event, color) {
    if (event == FourBeat.FB_EVENT_RELEASE) {
        switch (color) {
        case 'RED':
            //window.location.href = apps[index].href;
            loadgame(apps[index].id);
            break;
        case 'BLUE':
            if (index > 0) {
                apps[index].style.backgroundColor="#ffffff";
                index -= 1;
                apps[index].style.backgroundColor="#ff7070";
            }
            break
        case 'YELLOW':
            if (index < apps.length - 1) {
                apps[index].style.backgroundColor="#ffffff";
                index += 1;
                apps[index].style.backgroundColor="#ff7070";
            }
            break
        case 'GREEN':
            history.back();
            break;
        default:
            break;
        }
    }
});




