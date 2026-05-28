document.addEventListener("DOMContentLoaded", function () {
	if (typeof Chart === "undefined") {
		console.error("Chart.js chưa load!");
		return;
	}

	if (document.getElementById("revenueChart")) {
		initRevenueChart();
	}

	if (document.getElementById("dailyRevenueChart")) {
		initDailyChart();
		initMonthPicker();
	}

	if (document.getElementById("filterType")) {
		initStatusFilter();
	}
});

const chartColors = {
	blue: "#2563eb",
	blueSoft: "rgba(37, 99, 235, 0.14)",
	green: "#16a34a",
	greenSoft: "rgba(22, 163, 74, 0.14)",
	amber: "#f59e0b",
	red: "#dc2626",
	slate: "#475569",
	cyan: "#0891b2",
	purple: "#7c3aed",
	grid: "rgba(148, 163, 184, 0.22)",
	text: "#334155"
};

const statusLabels = {
	PENDING_PAYMENT: "Chờ thanh toán",
	PROCESSING: "Đang xử lý",
	ORDER_RECEIVED: "Xác nhận đơn",
	PRODUCT_PACKED: "Đã đóng gói",
	OUT_FOR_DELIVERY: "Đang giao",
	DELIVERED: "Đã giao",
	CANCELLED: "Đã huỷ"
};

function formatCurrency(value) {
	return Number(value || 0).toLocaleString("vi-VN") + "đ";
}

function getCommonOptions() {
	return {
		responsive: true,
		maintainAspectRatio: false,
		plugins: {
			legend: {
				labels: {
					color: chartColors.text,
					boxWidth: 12,
					boxHeight: 12,
					font: {
						size: 12,
						weight: "600"
					}
				}
			},
			tooltip: {
				backgroundColor: "#111827",
				padding: 12,
				titleFont: {
					size: 13,
					weight: "700"
				},
				bodyFont: {
					size: 13
				}
			}
		}
	};
}

function initRevenueChart() {
	const yearSelector = document.getElementById("yearSelector");
	const canvas = document.getElementById("revenueChart");

	if (!yearSelector || !canvas) return;

	const currentYear = new Date().getFullYear();
	yearSelector.value = currentYear;

	window.revenueChart = new Chart(canvas.getContext("2d"), {
		type: "bar",
		data: {
			labels: Array.from({ length: 12 }, (_, i) => `T${i + 1}`),
			datasets: [{
				label: "Doanh thu",
				data: Array(12).fill(0),
				backgroundColor: chartColors.blueSoft,
				borderColor: chartColors.blue,
				borderWidth: 2,
				borderRadius: 6,
				maxBarThickness: 42
			}]
		},
		options: {
			...getCommonOptions(),
			scales: {
				x: {
					grid: {
						display: false
					},
					ticks: {
						color: chartColors.text
					}
				},
				y: {
					beginAtZero: true,
					grid: {
						color: chartColors.grid
					},
					ticks: {
						color: chartColors.text,
						callback: value => formatCurrency(value)
					}
				}
			},
			plugins: {
				...getCommonOptions().plugins,
				tooltip: {
					...getCommonOptions().plugins.tooltip,
					callbacks: {
						label: context => " " + formatCurrency(context.raw)
					}
				}
			}
		}
	});

	function updateRevenueChart(year) {
		fetch(`/admin/monthly?year=${year}`)
			.then(res => res.json())
			.then(data => {
				const revenue = Array.from({ length: 12 }, (_, i) => data[i + 1] || 0);

				window.revenueChart.data.datasets[0].data = revenue;
				window.revenueChart.update();
			})
			.catch(err => console.error("Load monthly revenue failed:", err));
	}

	yearSelector.addEventListener("change", function () {
		updateRevenueChart(this.value || currentYear);
	});

	updateRevenueChart(currentYear);
}

function initDailyChart() {
	const canvas = document.getElementById("dailyRevenueChart");

	if (!canvas) return;

	window.dailyChart = new Chart(canvas.getContext("2d"), {
		type: "line",
		data: {
			labels: [],
			datasets: [{
				label: "Doanh thu",
				data: [],
				borderColor: chartColors.green,
				backgroundColor: chartColors.greenSoft,
				borderWidth: 3,
				pointRadius: 3,
				pointHoverRadius: 5,
				tension: 0.35,
				fill: true
			}]
		},
		options: {
			...getCommonOptions(),
			scales: {
				x: {
					grid: {
						display: false
					},
					ticks: {
						color: chartColors.text
					}
				},
				y: {
					beginAtZero: true,
					grid: {
						color: chartColors.grid
					},
					ticks: {
						color: chartColors.text,
						callback: value => formatCurrency(value)
					}
				}
			},
			plugins: {
				...getCommonOptions().plugins,
				tooltip: {
					...getCommonOptions().plugins.tooltip,
					callbacks: {
						label: context => " " + formatCurrency(context.raw)
					}
				}
			}
		}
	});

	window.updateDayChart = function (year, month) {
		fetch(`/admin/daily-revenue?year=${year}&month=${Number(month)}`)
			.then(res => res.json())
			.then(data => {
				const daysInMonth = new Date(Number(year), Number(month), 0).getDate();
				const labels = Array.from({ length: daysInMonth }, (_, i) => String(i + 1));
				const revenue = labels.map(day => data[day] || 0);

				window.dailyChart.data.labels = labels;
				window.dailyChart.data.datasets[0].data = revenue;
				window.dailyChart.update();
			})
			.catch(err => console.error("Load daily revenue failed:", err));
	};
}

function initMonthPicker() {
	const monthInput = document.getElementById("monthSelector");

	if (!monthInput || !window.updateDayChart) return;

	const today = new Date();
	const currentMonth = String(today.getMonth() + 1).padStart(2, "0");
	const currentYear = today.getFullYear();

	monthInput.value = `${currentYear}-${currentMonth}`;

	monthInput.addEventListener("change", function () {
		const [year, month] = this.value.split("-");
		window.updateDayChart(year, month);
	});

	window.updateDayChart(currentYear, currentMonth);
}

function initStatusFilter() {
	document.getElementById("filterType")?.addEventListener("change", handleFilterChange);
	document.getElementById("startDate")?.addEventListener("change", updateStatusChart);
	document.getElementById("endDate")?.addEventListener("change", updateStatusChart);

	handleFilterChange();
}

function handleFilterChange() {
	const filterTypeEl = document.getElementById("filterType");
	const inputLabel = document.getElementById("inputLabel");
	const startDate = document.getElementById("startDate");
	const endDate = document.getElementById("endDate");
	const separator = document.querySelector(".separator");

	if (!filterTypeEl || !inputLabel || !startDate || !endDate || !separator) {
		return;
	}

	const filterType = filterTypeEl.value;
	const today = new Date();
	const currentDate = toDateInputValue(today);
	const currentMonth = currentDate.slice(0, 7);

	if (filterType === "day") {
		inputLabel.textContent = "Chọn khoảng ngày";
		startDate.type = "date";
		endDate.type = "date";
		startDate.style.display = "block";
		endDate.style.display = "block";
		separator.style.display = "inline";
		startDate.value = currentDate;
		endDate.value = currentDate;
	} else if (filterType === "month") {
		inputLabel.textContent = "Chọn tháng";
		startDate.type = "month";
		startDate.style.display = "block";
		endDate.style.display = "none";
		separator.style.display = "none";
		startDate.value = currentMonth;
	} else {
		inputLabel.textContent = "Chọn năm";
		startDate.type = "number";
		startDate.min = "2000";
		startDate.max = "2100";
		startDate.value = today.getFullYear();
		endDate.style.display = "none";
		separator.style.display = "none";
	}

	updateStatusChart();
}

function toDateInputValue(date) {
	const year = date.getFullYear();
	const month = String(date.getMonth() + 1).padStart(2, "0");
	const day = String(date.getDate()).padStart(2, "0");

	return `${year}-${month}-${day}`;
}

async function updateStatusChart() {
	const filterType = document.getElementById("filterType")?.value;
	const startDateInput = document.getElementById("startDate");
	const endDateInput = document.getElementById("endDate");

	if (!filterType || !startDateInput) return;

	let startDate;
	let endDate;

	if (filterType === "day") {
		startDate = `${startDateInput.value}T00:00:00`;
		endDate = `${endDateInput.value}T23:59:59`;
	} else if (filterType === "month") {
		const [year, month] = startDateInput.value.split("-");
		const lastDay = new Date(Number(year), Number(month), 0).getDate();

		startDate = `${year}-${month}-01T00:00:00`;
		endDate = `${year}-${month}-${String(lastDay).padStart(2, "0")}T23:59:59`;
	} else {
		startDate = `${startDateInput.value}-01-01T00:00:00`;
		endDate = `${startDateInput.value}-12-31T23:59:59`;
	}

	try {
		const data = await fetch(`/admin/statistics?startDate=${startDate}&endDate=${endDate}`)
			.then(res => res.json());

		renderStatusChart(data);
	} catch (error) {
		console.error("Load order status failed:", error);
		renderStatusChart({});
	}
}

function renderStatusChart(data) {
	const canvas = document.getElementById("orderStatusChart");

	if (!canvas) return;

	const rawLabels = Object.keys(data || {});
	const values = Object.values(data || {});

	const labels = rawLabels.map(label => statusLabels[label] || label);
	const total = values.reduce((sum, value) => sum + Number(value || 0), 0);

	if (window.statusChart) {
		window.statusChart.destroy();
	}

	window.statusChart = new Chart(canvas.getContext("2d"), {
		type: "doughnut",
		data: {
			labels: total > 0 ? labels : ["Chưa có dữ liệu"],
			datasets: [{
				data: total > 0 ? values : [1],
				backgroundColor: total > 0
					? [chartColors.slate, chartColors.amber, chartColors.cyan, chartColors.blue, chartColors.purple, chartColors.green, chartColors.red]
					: ["#e5e7eb"],
				borderWidth: 0,
				hoverOffset: 8
			}]
		},
		options: {
			...getCommonOptions(),
			cutout: "62%",
			plugins: {
				...getCommonOptions().plugins,
				tooltip: {
					...getCommonOptions().plugins.tooltip,
					callbacks: {
						label: context => {
							if (total === 0) return " Chưa có dữ liệu";

							const value = Number(context.raw || 0);
							const percent = total === 0 ? 0 : Math.round((value / total) * 100);

							return ` ${value} đơn (${percent}%)`;
						}
					}
				}
			}
		}
	});
}
