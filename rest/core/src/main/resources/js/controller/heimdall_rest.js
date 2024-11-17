Application.bind("rest.project.sync", function () {
    DataSet.post("sync", {}, function (json) {
        Application.showInfoAlert("Project", json.message)
    }, {dataType: "json"});
});

Application.bind("rest.library.design", function () {

});

Application.bind("rest.simulation.design", function (id) {

});

Application.bind("rest.schedule.run", function () {
    Logger.info("Start simulation with id '" + DataSet.getId() + "'");
    DataSet.post("schedule/" + DataSet.getId(), {}, function (json) {
        Application.showInfoAlert("Simulation", json.message)
    }, {dataType: "json"});
});