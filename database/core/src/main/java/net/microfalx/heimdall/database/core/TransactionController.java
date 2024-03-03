package net.microfalx.heimdall.database.core;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("heimdallDatabaseTransactionController")
@RequestMapping("database/transaction")
public class TransactionController extends net.microfalx.bootstrap.web.controller.admin.database.TransactionController {
}
