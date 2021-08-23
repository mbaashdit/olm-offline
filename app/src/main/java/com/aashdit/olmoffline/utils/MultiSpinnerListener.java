package com.aashdit.olmoffline.utils;


import com.aashdit.olmoffline.models.IrrigationSource;

import java.util.List;

public interface MultiSpinnerListener {
	void onItemsSelected(List<IrrigationSource> selectedItems);
}
