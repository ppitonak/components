/*
 * JBoss, Home of Professional Open Source
 * Copyright ${year}, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */ 
(function(richfaces, jQuery) {
	richfaces.getCSSRule = function (className) {
		var rule = null;
		var sheets = document.styleSheets;
		for (var j = 0; !rule && j < sheets.length; j++) {
			var rules = sheets[j].cssRules ? sheets[j].cssRules: sheets[j].rules;
			for (var i = 0; !rule && i < rules.length; i++) {
				if (rules[i].selectorText && rules[i].selectorText.toLowerCase() == className.toLowerCase()) {
					rule = rules[i];
				}
			}
		}
		return rule;			
	};

	richfaces.ExtendedDataTable = function(id, rowCount, ajaxFunction, ajaxParameters) {
		var WIDTH_CLASS_NAME_BASE = "rf-edt-cw-";
		var MIN_WIDTH = 20;
		
		var element = document.getElementById(id);
		var bodyElement, contentElement, spacerElement, dataTableElement, rows, rowHeight, parts;
		var dragElement = document.getElementById(id + ":d");
		var reorderElement = document.getElementById(id + ":r");
		var reorderMarkerElement = document.getElementById(id + ":rm");
		var widthInput = document.getElementById(id + ":wi");
		var normalPartStyle = richfaces.getCSSRule(".rf-edt-pw").style;
		var header = jQuery(element).children(".rf-edt-h");
		var resizerHolders = header.find(".rf-edt-rsh").get();
		
		var frozenHeaderPartElement = document.getElementById(id + ":frozenHeader");
		var frozenColumnCount = frozenHeaderPartElement ? frozenHeaderPartElement.firstChild.rows[0].cells.length : 0;//TODO Richfaces.firstDescendant;
		
		var scrollElement = document.getElementById(id + ":footer");
		
		var resizeData = {};
		var idOfReorderingColumn = "";
		var newWidths = {};
		
		var timeoutId = null;
		
		var sendAjax = function(event, map) {
			for (key in ajaxParameters) {
				if(!map[key]) {
					map[key] = ajaxParameters[key];
				}
			}
			ajaxFunction(event, map);
		};
		
		var updateLayout = function() {
			var offsetWidth = frozenHeaderPartElement ? frozenHeaderPartElement.offsetWidth : 0;
			var width = Math.max(0, element.clientWidth - offsetWidth);
			if (width) {
				normalPartStyle.width = width + "px";
				normalPartStyle.display = "block";
				if (scrollElement.clientWidth < scrollElement.scrollWidth
						&& scrollElement.scrollHeight == scrollElement.offsetHeight) {
					scrollElement.style.overflowX = "scroll";
				} else {
					scrollElement.style.overflowX = "";
				}
				var delta = scrollElement.firstChild.offsetHeight - scrollElement.clientHeight;
				if (delta) {
					scrollElement.style.height = scrollElement.offsetHeight + delta;
				}
			} else {
				normalPartStyle.display = "none";
			}
			var height = element.clientHeight;
			var el = element.firstChild;
			while (el) {
				if(el.nodeName && el.nodeName.toUpperCase() == "DIV" && el != bodyElement) {
					height -= el.offsetHeight;
				}
				el = el.nextSibling;
			}
			bodyElement.style.height = height + "px";
		};
		
		var adjustResizers = function() {
			var scrollLeft = scrollElement.scrollLeft;
			var clientWidth = element.clientWidth - 3;
			var i = 0;
			for (; i < frozenColumnCount; i++) {
				if (clientWidth > 0) {
					resizerHolders[i].style.display = "none";
					resizerHolders[i].style.display = "";
					clientWidth -= resizerHolders[i].offsetWidth;
				}
				if (clientWidth <= 0) {
					resizerHolders[i].style.display = "none";
				}
			}
			scrollLeft -= 3;
			for (; i < resizerHolders.length; i++) {
				if (clientWidth > 0) {
					resizerHolders[i].style.display = "none";
					if (scrollLeft > 0) {
						resizerHolders[i].style.display = "";
						scrollLeft -= resizerHolders[i].offsetWidth;
						if (scrollLeft > 0) {
							resizerHolders[i].style.display = "none";
						} else {
							clientWidth += scrollLeft;
						}
					} else {
						resizerHolders[i].style.display = "";
						clientWidth -= resizerHolders[i].offsetWidth;
					}
				}
				if (clientWidth <= 0) {
					resizerHolders[i].style.display = "none";
				}
			}
		};

		var updateScrollPosition = function() {
			var scrollLeft = scrollElement.scrollLeft;
			parts.each(function() {
				this.scrollLeft = scrollLeft;
			});
			adjustResizers();
		};

		var initializeLayout = function() {
			bodyElement = document.getElementById(id + ":b");
			contentElement = jQuery(bodyElement).children("div:first").get(0);
			if (contentElement) {
				spacerElement = contentElement.firstChild;//TODO this.marginElement = Richfaces.firstDescendant(this.contentElement);
				dataTableElement = contentElement.lastChild;//TODO this.dataTableElement = Richfaces.lastDescendant(this.contentElement);
				rows = document.getElementById(id + ":body").firstChild.rows.length;//TODO Richfaces.firstDescendant;
				rowHeight = dataTableElement.offsetHeight / rows;
				contentElement.style.height = (rowCount * rowHeight) + "px";
				jQuery(bodyElement).bind("scroll", bodyScrollListener);
			} else {
				spacerElement = null;
				dataTableElement = null;
			}
			parts = jQuery(element).find(".rf-edt-p");
			updateLayout();
			updateScrollPosition(); //TODO Restore horizontal scroll position
		};
		
		var drag = function(event) {
			jQuery(dragElement).setPosition({left:Math.max(resizeData.left + MIN_WIDTH, event.pageX)});
			return false;
		};
		
		var beginResize = function(event) {
			var id = this.parentNode.className.match(new RegExp(WIDTH_CLASS_NAME_BASE + "([^\\W]*)"))[1];
			resizeData = {
				id : id,
				left : jQuery(this).parent().offset().left
			};
			dragElement.style.height = element.offsetHeight + "px";
			jQuery(dragElement).setPosition({top:jQuery(element).offset().top, left:event.pageX});
			dragElement.style.display = "block";
			jQuery(document).bind("mousemove", drag);
			jQuery(document).one("mouseup", endResize);
			return false;
		};
		
		var setColumnWidth = function(id, width) {
			width = width + "px";
			richfaces.getCSSRule("." + WIDTH_CLASS_NAME_BASE + id).style.width = width;
			newWidths[id] = width;
			var widthsArray = new Array();
			for (var id in newWidths) {
				widthsArray.push(id + ":" + newWidths[id]);
			}
			widthInput.value = widthsArray.toString();
			updateLayout();
			adjustResizers();
			sendAjax(); // TODO Maybe, event model should be used here.
		};

		var endResize = function(event) {
			jQuery(document).unbind("mousemove", drag);
			dragElement.style.display = "none";
			var width = Math.max(MIN_WIDTH, event.pageX - resizeData.left);
			setColumnWidth(resizeData.id, width);
		};

		var reorder = function(event) {
			jQuery(reorderElement).setPosition(event, {offset:[5,5]});
			reorderElement.style.display = "block";
			return false;
		};

		var beginReorder = function(event) {
			idOfReorderingColumn = this.className.match(new RegExp(WIDTH_CLASS_NAME_BASE + "([^\\W]*)"))[1];
			jQuery(document).bind("mousemove", reorder);
			header.find(".rf-edt-hc").bind("mouseover", overReorder);
			jQuery(document).one("mouseup", cancelReorder);
			return false;
		};
		
		var overReorder = function(event) {
			if (idOfReorderingColumn != this.className.match(new RegExp(WIDTH_CLASS_NAME_BASE + "([^\\W]*)"))[1]) {
				var thisElement = jQuery(this);
				var offset = thisElement.offset();
				jQuery(reorderMarkerElement).setPosition({top:offset.top + thisElement.height(), left:offset.left - 5});
				reorderMarkerElement.style.display = "block";
				thisElement.one("mouseout", outReorder);
				thisElement.one("mouseup", endReorder);
			}
		};
		
		var outReorder = function(event) {
			reorderMarkerElement.style.display = "";
			jQuery(this).unbind("mouseup", endReorder);
		};
		
		var endReorder = function(event) {
			reorderMarkerElement.style.display = "";
			jQuery(this).unbind("mouseout", outReorder);
			var id = this.className.match(new RegExp(WIDTH_CLASS_NAME_BASE + "([^\\W]*)"))[1];
			var colunmsOrder = "";
			header.find(".rf-edt-hc").each(function() {
				var i = this.className.match(new RegExp(WIDTH_CLASS_NAME_BASE + "([^\\W]*)"))[1];
				if (i == id) {
					colunmsOrder += idOfReorderingColumn + "," + id + ",";
				} else if (i != idOfReorderingColumn) {
					colunmsOrder += i + ",";
				}
			});
			sendAjax(event, {"rich:columnsOrder" : colunmsOrder}); // TODO Maybe, event model should be used here.
		};
		
		var cancelReorder = function(event) {
			jQuery(document).unbind("mousemove", reorder);
			header.find(".rf-edt-hc").unbind("mouseover", overReorder);
			reorderElement.style.display = "none";
		};
		
		var loadData = function(event) {
			var clientFirst = Math.round((bodyElement.scrollTop + bodyElement.clientHeight / 2) / (rowHeight) - rows / 2);
			if (clientFirst <= 0) {
				clientFirst = 0;
			} else {
				clientFirst = Math.min(rowCount - rows, clientFirst);
			}
			sendAjax(event, {"rich:clientFirst" : clientFirst});// TODO Maybe, event model should be used here.
		}

		var bodyScrollListener = function(event) {
			if(timeoutId) {
				window.clearTimeout(timeoutId);
				timeoutId = null;
			}
			if (this.scrollTop < spacerElement.offsetHeight || this.scrollTop + this.clientHeight > spacerElement.offsetHeight + dataTableElement.offsetHeight) {
				timeoutId = window.setTimeout(function (event) {loadData(event)}, 1000);
			}
		};

		var ajaxComplete = function (event, data) {
			if (data.reinitializeHeader) {
				bindHeaderHandlers();
			} else {
				if (data.reinitializeBody) {
					rowCount = data.rowCount;
					initializeLayout();
				}
				if (spacerElement) {
					spacerElement.style.height = (data.first * rowHeight) + "px";
				}
			}
		};
		
		jQuery(document).ready(initializeLayout);
		jQuery(window).bind("resize", updateLayout);
		jQuery(scrollElement).bind("scroll", updateScrollPosition);
		var bindHeaderHandlers = function () {
			header.find(".rf-edt-rs").bind("mousedown", beginResize);
			header.find(".rf-edt-hc").bind("mousedown", beginReorder);
		}
		bindHeaderHandlers();
		jQuery(bodyElement).bind("scroll", bodyScrollListener);
		jQuery(element).bind("rich:onajaxcomplete", ajaxComplete);
		
		//JS API
		element["richfaces"] = element["richfaces"] || {}; // TODO ExtendedDataTable should extend richfaces.BaseComponent instead of using it.
		element.richfaces.component = this;
		
		this.getColumnPosition = function(id) {
			var position;
			var headers = header.find(".rf-edt-hc");
			for (var i = 0; i < headers.length; i++) {
				if (id == headers.get(i).className.match(new RegExp(WIDTH_CLASS_NAME_BASE + "([^\\W]*)"))[1]) {
					position = i;
				}
			}
			return position;
		}
		
		this.setColumnPosition = function(id, position) {
			var colunmsOrder = "";
			var before;
			var headers = header.find(".rf-edt-hc");
			for (var i = 0; i < headers.length; i++) {
				var current = headers.get(i).className.match(new RegExp(WIDTH_CLASS_NAME_BASE + "([^\\W]*)"))[1];
				if (i == position) {
					if (before) {
						colunmsOrder += current + "," + id + ",";
					} else {
						colunmsOrder += id + "," + current + ",";
					}
				} else {
					if (id != current) {
						colunmsOrder += current + ",";
					} else {
						before = true;
					}
				}
			}
			sendAjax(null, {"rich:columnsOrder" : colunmsOrder}); // TODO Maybe, event model should be used here.
		}
		
		this.setColumnWidth = function(id, width) {
			setColumnWidth(id, width);
		}
		
		this.filter = function(colunmId, filterValue, isClear) {
			if (typeof(filterValue) == "undefined" || filterValue == null) {
				filterValue = "";
			}
			var map = {}
			map[id + "rich:filtering"] = colunmId + ":" + filterValue + ":" + isClear;
			sendAjax(null, map); // TODO Maybe, event model should be used here.
		}
		
		this.clearFiltering = function() {
			this.filter("", "", true);
		}
		
		this.sort = function(colunmId, sortOrder, isClear) {
			if (typeof(sortOrder) == "string") {
				sortOrder = sortOrder.toUpperCase();
			}
			var map = {}
			map[id + "rich:sorting"] = colunmId + ":" + sortOrder + ":" + isClear;
			sendAjax(null, map); // TODO Maybe, event model should be used here.
		}
		
		this.clearSorting = function() {
			this.filter("", "", true);
		}
	};
}(window.RichFaces, jQuery));

