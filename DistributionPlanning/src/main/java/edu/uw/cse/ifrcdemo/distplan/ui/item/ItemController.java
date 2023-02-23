/*
 * Copyright (c) 2016-2022 University of Washington
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of Washington nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY OF WASHINGTON AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF WASHINGTON OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package edu.uw.cse.ifrcdemo.distplan.ui.item;

import edu.uw.cse.ifrcdemo.distplan.entity.ItemPack;
import edu.uw.cse.ifrcdemo.distplan.model.itempack.ItemPackRepository;
import edu.uw.cse.ifrcdemo.distplan.util.FxDialogUtil;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.PersistenceException;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/item")
public class ItemController {
  private static final String ITEM_NEW_ITEM = "item/newItem";
  private static final String ITEM_VIEW_ITEMS = "item/viewItems";
  private static final String ITEM_EDIT_ITEM = "item/editItem";

  private final ItemPackRepository itemPackRepository;
  private final Logger logger;

  public ItemController(ItemPackRepository itemPackRepository, Logger logger) {
    this.itemPackRepository = itemPackRepository;
    this.logger = logger;
  }

  @ModelAttribute
  public NewItemFormModel getNewItemFormModel() {
    return new NewItemFormModel();
  }

  @GetMapping("new/auth")
  public ModelAndView newItem(@ModelAttribute NewItemFormModel form) {
    return new ModelAndView(ITEM_NEW_ITEM);
  }

  @GetMapping("new/other")
  public ModelAndView newItemOther(@ModelAttribute NewItemFormModel form) {
    return new ModelAndView(ITEM_NEW_ITEM);
  }

  @PostMapping("new/{page}")
  public String newItemPostPage(@ModelAttribute NewItemFormModel form, @PathVariable String page) {
    ItemPack newItem = new ItemPack();
    newItem.setName(form.getName());
    newItem.setDescription(form.getDescription());

    itemPackRepository.saveItemPack(newItem).join();

    if (page.equals("other")) {
      FxDialogUtil.showInfoDialog(String
          .format(TranslationUtil.getTranslations().getString(TranslationConsts.CREATED_ITEM),
              form.getName()));
      return "redirect:/other";
    } else {
      return "redirect:/authorization/new";
    }
  }

  @GetMapping("view")
  public ModelAndView listItem() {
    List<ItemListModel> items = itemPackRepository
        .getItemPackList()
        .stream()
        .map(ItemListModel::new)
        .collect(Collectors.toList());

    ModelAndView modelAndView = new ModelAndView(ITEM_VIEW_ITEMS);
    modelAndView.addObject("itemList", items);
    modelAndView.addObject(new EditItemFormModel());

    return modelAndView;
  }

  @GetMapping("edit/{rowId}")
  public ModelAndView editItem(@PathVariable("rowId") String rowId) {
    EditItemFormModel editItemFormModel = new EditItemFormModel();

    ItemPack itemPackByRowId = itemPackRepository.getItemPackByRowId(rowId);
    editItemFormModel.setName(itemPackByRowId.getName());
    editItemFormModel.setDescription(itemPackByRowId.getDescription());

    ModelAndView modelAndView = new ModelAndView(ITEM_EDIT_ITEM);
    modelAndView.addObject(editItemFormModel);

    return modelAndView;
  }

  @PostMapping("edit/{rowId}")
  public ModelAndView editItemPost(@PathVariable("rowId") String rowId,
                                   @Valid @ModelAttribute EditItemFormModel editItemFormModel,
                                   BindingResult bindingResult) {

    if (!bindingResult.hasErrors()) {
      try {
        itemPackRepository.editItemPack(
            rowId,
            editItemFormModel.getName(),
            editItemFormModel.getDescription()
        );

        return new ModelAndView("redirect:/item/view", "clearHistory", "/item/view");
      } catch (PersistenceException e) {
        if (!(e.getCause() instanceof ConstraintViolationException)) {
          throw e;
        }

        if (e.getCause() instanceof ConstraintViolationException) {
          FieldError fieldError = new FieldError(
              "editItemFormModel",
              "name",
              editItemFormModel.getName(),
              false,
              null,
              null,
              TranslationUtil.getTranslations().getString(TranslationConsts.DELIVERABLE_ITEM_UNIQ_NAME_ERROR)
          );
          bindingResult.addError(fieldError);
        }
      }
    }

    ModelAndView modelAndView = new ModelAndView(ITEM_EDIT_ITEM);
    modelAndView.addObject(editItemFormModel);

    return modelAndView;
  }
}
