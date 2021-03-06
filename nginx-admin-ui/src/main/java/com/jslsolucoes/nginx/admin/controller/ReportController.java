package com.jslsolucoes.nginx.admin.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.jslsolucoes.nginx.admin.error.NginxAdminException;
import com.jslsolucoes.nginx.admin.model.Nginx;
import com.jslsolucoes.nginx.admin.model.VirtualHostAlias;
import com.jslsolucoes.nginx.admin.repository.ReportRepository;
import com.jslsolucoes.nginx.admin.repository.VirtualHostAliasRepository;
import com.jslsolucoes.tagria.lib.form.FormValidation;

import br.com.caelum.vraptor.Controller;
import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.view.Results;

@Controller
@Path("report")
public class ReportController {

	private Result result;
	private ReportRepository reportRepository;
	private VirtualHostAliasRepository virtualHostAliasRepository;
	private HttpServletResponse httpServletResponse;

	@Deprecated
	public ReportController() {

	}

	@Inject
	public ReportController(Result result, ReportRepository reportRepository,
			VirtualHostAliasRepository virtualHostAliasRepository, HttpServletResponse httpServletResponse) {
		this.result = result;
		this.reportRepository = reportRepository;
		this.virtualHostAliasRepository = virtualHostAliasRepository;
		this.httpServletResponse = httpServletResponse;
	}

	public void validate(List<Long> aliases, LocalDate from, LocalTime fromTime, LocalDate to, LocalTime toTime,
			Long idNginx) {
		this.result.use(Results.json())
				.from(FormValidation.newBuilder().toUnordenedList(reportRepository
						.validateBeforeSearch(convert(aliases), from, fromTime, to, toTime, new Nginx(idNginx))),
						"errors")
				.serialize();
	}

	@Path("search/{idNginx}")
	public void search(Long idNginx) {
		this.result.include("virtualHostAliasList", virtualHostAliasRepository.listAllFor(new Nginx(idNginx)));
		this.result.include("nginx", new Nginx(idNginx));
	}

	@Post
	@Path("export.pdf")
	public void export(List<Long> aliases, LocalDate from, LocalTime fromTime, LocalDate to, LocalTime toTime,
			Long idNginx) throws NginxAdminException, IOException {
		httpServletResponse.setContentType("application/pdf");
		IOUtils.copy(reportRepository.statistics(convert(aliases), from, fromTime, to, toTime, new Nginx(idNginx)),
				httpServletResponse.getOutputStream());
		this.result.use(Results.status()).ok();
	}

	private List<VirtualHostAlias> convert(List<Long> aliases) {
		if (aliases == null)
			return new ArrayList<>();
		List<VirtualHostAlias> virtualHostAliases = new ArrayList<>();
		for (Long idVirtualHostAlias : aliases) {
			virtualHostAliases.add(new VirtualHostAlias(idVirtualHostAlias));
		}
		return virtualHostAliases;
	}
}
