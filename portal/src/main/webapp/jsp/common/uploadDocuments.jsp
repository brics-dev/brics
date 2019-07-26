<%@include file="/common/taglibs.jsp"%>


		<c:if test="${not empty uploadedFiles}">
			<table class="display-data full-width" id="filesList">
				<thead>
					<tr>
						<th>File Name</th>
						<th>File Type</th>
						<th>Date uploaded</th>
						<th>Remove File?</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="uploadedFile" items="${uploadedFiles}" varStatus="status">
				 		<c:choose>
							<c:when test="${status.count%2 == 0}">
								<tr class="stripe">
									<td>${uploadedFile.name}</td>
									<td>${uploadedFile.description}</td>
									<td>${uploadedFile.uploadDateString}</td>
									<td><a href="javascript: removeFile('${uploadedFile.name}')">remove</a></td>
								</tr>
							</c:when>
							<c:otherwise>
								<tr>
									<td>${uploadedFile.name}</td>
									<td>${uploadedFile.description}</td>
									<td>${uploadedFile.uploadDateString}</td>
									<td><a href="javascript: removeFile('${uploadedFile.name}')">remove</a></td>
								</tr>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</tbody>
			</table>
		</c:if>


