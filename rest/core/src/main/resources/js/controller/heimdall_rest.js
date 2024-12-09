Application.bind("rest.project.sync", function () {
    DataSet.post("sync", {}, function (json) {
        Application.showInfoAlert("Project", json.message)
    }, {dataType: "json"});
});

Application.bind("rest.library.history", function (id) {
    DataSet.get("history/" + id, {}, function (data) {
        Application.loadModal("history-modal", data);
    });
});

Application.bind("rest.simulation.history", function (id) {
    DataSet.get("history/" + id, {}, function (data) {
        Application.loadModal("history-modal", data);
    });
});

Application.bind("rest.library.design", function () {
    CodeEditor.loadModal("design/" + DataSet.getId());
});

Application.bind("rest.simulation.design", function () {
    CodeEditor.loadModal("design/" + DataSet.getId());
});

Application.bind("rest.simulation.view.logs", function (id) {
    DataSet.get("log/" + id, {}, function (data) {
        Application.loadModal("log-modal", data);
    });
});

Application.bind("rest.simulation.view.data", function (id) {
    DataSet.get("data/" + id, {}, function (data) {
        Application.loadModal("data-modal", data);
    });
});

Application.bind("rest.simulation.view.report", function (id) {
    DataSet.get("report/" + id, {}, function (data) {
        Application.loadModal("report-modal", data);
    });
});

Application.bind("rest.schedule.run", function () {
    Logger.info("Start simulation with id '" + DataSet.getId() + "'");
    DataSet.post("run/" + DataSet.getId(), {}, function (json) {
        Application.showInfoAlert("Simulation", json.message)
    }, {dataType: "json"});
});