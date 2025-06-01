Application.bind("smtp.forward", function (id) {
    DataSet.post(id + "/forward", {}, function (json) {
        Application.showAlert("SMTP", json.message, json.success);
    }, {dataType: "json"});
});