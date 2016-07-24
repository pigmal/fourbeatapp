function loadgame(id) {
    if (typeof FbNativeInterface === 'undefined') {
        console.log("FbNativeInterface not found");
    } else {
        FbNativeInterface.loadGame(id);
    }
}
